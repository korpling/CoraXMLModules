package org.corpus_tools.peppermodules.coraXMLModules;

import java.util.Queue;
import java.util.LinkedList;
import java.util.Map;
import java.util.Hashtable;
import java.util.Stack;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SAnnotation;
import org.xml.sax.Attributes;

class Text {
    protected class TextLayer {
        private STextualDS textual;
        private SDocumentGraph graph;
        private String layer_name;
        protected String seg_name;
        private SToken last_added;
        private Integer current_position = 0;
        /// all open dipl/mod which are related to a link and
        /// are not connected with other SToken objects so far
        protected Queue<SToken> open_tokens = new LinkedList<>();
        /// stores offsets of mod/dipl-Tokens in the current token
        protected Queue<Integer> offsets = new LinkedList<>();

        protected TextLayer(String l, SDocumentGraph my_graph) {
            layer_name = l;
            graph = my_graph;
            textual = SaltFactory.createSTextualDS();
            textual.setText("");
            graph.addNode(textual);
        }
        public TextLayer set_seg(String name) {
            seg_name = name;
            return this;
        }
        void set_start(Integer pos) { current_position = pos; }

        protected int last_offset() {
            if (offsets.isEmpty())
                return 0;
            return offsets.size() - 1;
        }
        public SToken last_token() { return last_added; }
        public TextLayer add_token(Attributes attr) {
            int left_pos = textual.getText().length();
            String value
                = StringEscapeUtils.unescapeHtml4(attr.getValue(layer_name));
            textual.setText(textual.getText() + value);
            int right_pos = left_pos + value.length();
            SToken tok = graph.createToken(textual, left_pos, right_pos);
            order(last_added, tok);
            if (open_tokens != null && offsets != null) {
                open_tokens.add(tok);
                offsets.add(last_offset() + value.length());
            }
            last_added = tok;
            nodestack.add(last_added);
            textual.setText(textual.getText() + " ");

            return this;
        }
        private void order(SToken source, SToken target) {
            if (source == null)
                return;
            SOrderRelation rel = SaltFactory.createSOrderRelation();
            rel.setSource(source);
            rel.setTarget(target);
            rel.setType(seg_name);
            graph.addRelation(rel);
        }
        public void tok_to_timeline(STimeline timeline, Integer end) {
            SToken tok = open_tokens.poll();
            if (tok == null)
                return;
            STimelineRelation rel = SaltFactory.createSTimelineRelation();
            rel.setTarget(timeline);
            rel.setSource(tok);
            rel.setStart(current_position);
            rel.setEnd(end);
            graph.addRelation(rel);
            current_position = end;
        }
        public SAnnotation annotate(String name, String value) {
            if (nodestack.peek() == null)
                return null;
            return nodestack.peek().createAnnotation("annotation", name, value);
        }
        /// finish annotating last added token
        public void finalize() {
            nodestack.pop();
        }
    }

    private SDocumentGraph graph;
    private Stack<SNode> nodestack = new Stack<>();
    /// key is segmentation name, i.e. how the row will be named in annis
    private Map<String, TextLayer> sub_layers
        = new Hashtable<>();
    private TextLayer tok_layer;
    private STimeline timeline;

    private TextLayer make_layer(String layer_name) {
        return new TextLayer(layer_name, graph);
    }
    private Text() {}
    public Text(SDocumentGraph the_graph,
                String tok_dipl_textlayer,
                String tok_mod_textlayer) {
        graph = the_graph;
        tok_layer = make_layer("trans").set_seg("token");
        tok_layer.seg_name = "token";
        tok_layer.open_tokens = null;
        tok_layer.offsets = null;
        //           cora tagname        layername         seg_name
        sub_layers.put("dipl", make_layer(tok_dipl_textlayer).set_seg("tok_dipl"));
        sub_layers.put("mod", make_layer(tok_mod_textlayer).set_seg("tok_mod"));
        timeline = SaltFactory.createSTimeline();
        graph.setTimeline(timeline);
        // add initial PointOfTime
        timeline.increasePointOfTime();
    }

    public TextLayer layer(String coraxml_name) {
        return sub_layers.get(coraxml_name);
    }
    public void do_export_tokenlayer(boolean what) {
        does_export_tokenlayer = what;
    }
    private boolean does_export_tokenlayer = false;

    public void annotate(String name, Attributes attr) {
        layer("mod").annotate(name, attr.getValue("tag"));
    }
    public void annotate(String name, String value) {
        layer("mod").annotate(name, value);
    }

    /// returns true when both subordinate sub_layers are empty
    private boolean no_opens() {
        for (TextLayer layer : sub_layers.values())
            if (layer.open_tokens.peek() != null)
                return false;
        return true;
    }
    public void map_tokens_to_timeline_simple() {
        Integer start = timeline.getEnd() - 1;
        for (TextLayer l : sub_layers.values())
            l.set_start(start);
        while (!no_opens()) {
            timeline.increasePointOfTime();
            Integer end = timeline.getEnd() - 1;
            for (TextLayer l : sub_layers.values())
                l.tok_to_timeline(timeline, end);
        }
        timeline.increasePointOfTime();
        int end = timeline.getEnd() - 1;
        if (does_export_tokenlayer) {
            tok_layer.set_start(start);
            tok_layer.tok_to_timeline(timeline, end);
        }
    }
    public void map_tokens_to_timeline_aligned() {
        SortedSet<Integer> tok_offsets = new TreeSet<Integer>();
        tok_offsets.addAll(layer("dipl").offsets);
        tok_offsets.addAll(layer("mod").offsets);

        int start = timeline.getEnd() - 1;
        layer("dipl").set_start(start);
        layer("mod").set_start(start);

        for (Integer tok_offset : tok_offsets) {
            timeline.increasePointOfTime();
            int end = timeline.getEnd() - 1;
            if (tok_offset == layer("dipl").offsets.peek()) {
                layer("dipl").tok_to_timeline(timeline, end);
                layer("dipl").offsets.remove();
            }
            if (tok_offset == layer("mod").offsets.peek()) {
                layer("mod").tok_to_timeline(timeline, end);
                layer("mod").offsets.remove();
            }
        }
        if (does_export_tokenlayer) {
            tok_layer.set_start(start);
            tok_layer.tok_to_timeline(timeline,
                                      timeline.getEnd() - 1);
        }
    }
}

