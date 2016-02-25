/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.coraXMLModules;

import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringEscapeUtils;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SOrderRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
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
        private SToken last_added = null;
        private Integer current_position = 0;
        /// all open dipl/mod which are related to a link and
        /// are not connected with other SToken objects so far
        protected Queue<SToken> open_tokens = new LinkedList<>();
        /// stores offsets of mod/dipl-Tokens in the current token
        protected List<Integer> offsets = new LinkedList<>();

        protected TextLayer(String l, SDocumentGraph my_graph) {
            layer_name = l;
            graph = my_graph;
            textual = SaltFactory.createSTextualDS();
            textual.setText("");
            graph.addNode(textual);
        }
        public void set_seg(String name) {
            seg_name = name;
        }
        void set_start(Integer pos) { current_position = pos; }

        protected int last_offset() {
            if (offsets.isEmpty())
                return 0;
            return offsets.get(offsets.size()-1);
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
                offsets.add(last_offset() + attr.getValue("trans").length());
            }
            last_added = tok;
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
    }

    // a TextLayer that allows annotating the tokens
    protected class ModLayer extends TextLayer {

        private List<SToken> open_tokens = new LinkedList<>();
        private Map<String, SSpan> span_annotations = new HashMap<String, SSpan>();

        protected ModLayer(String l, SDocumentGraph my_graph) {
            super(l, my_graph);
        }

        @Override
        public ModLayer add_token(Attributes attr) {

            super.add_token(attr);
            open_tokens.add(this.last_token());

            return this;
        }

        public void add_to_span(String id) {

            if (this.last_token() == null)
                return;

            SSpan span;
            if(this.span_annotations.containsKey(id)) {
                span = this.span_annotations.get(id);
            }
            else {
                span = SaltFactory.createSSpan();
                graph.addNode(span);
                this.span_annotations.put(id, span);
            }

            SSpanningRelation rel = SaltFactory.createSSpanningRelation();
            rel.setSource(span);
            rel.setTarget(this.last_token());
            graph.addRelation(rel);

        }

        public void annotate_span(String id, String name, String value) {
            if (this.span_annotations.containsKey(id)) {
                this.span_annotations.get(id).createAnnotation("annotation", name, value);
            }
        }

        public void annotate_boundary(String name, String value) {

            SSpan span = SaltFactory.createSSpan();
            span.createAnnotation("annotation", name, value);
            graph.addNode(span);

            for (SToken tok: open_tokens) {
                SSpanningRelation rel = SaltFactory.createSSpanningRelation();
                rel.setSource(span);
                rel.setTarget(tok);
                graph.addRelation(rel);
            }

            open_tokens.clear();

        }

        public SAnnotation annotate(String name, String value) {
            if (this.last_token() == null)
                return null;
            return last_token().createAnnotation("annotation", name, value);
        }

    }

    private SDocumentGraph graph;
    /// key is segmentation name, i.e. how the row will be named in annis
    private Map<String, TextLayer> sub_layers
        = new Hashtable<>();
    private ModLayer mod_layer;


    private STimeline timeline;

    private TextLayer make_layer(String layer_name) {
        return new TextLayer(layer_name, graph);
    }
    private ModLayer make_annotatable_layer(String layer_name) {
        return new ModLayer(layer_name, graph);
    }

    private Text() {}
    public Text(SDocumentGraph the_graph,
                String tok_dipl_textlayer,
                String tok_mod_textlayer, boolean export_token_layer) {

        graph = the_graph;

        // create the text layers:
        // make_layer("cora_tagname")
        // set_seg("seg_name")
        if (export_token_layer) {
            TextLayer tok_layer = make_layer("trans");
            tok_layer.set_seg("token");
            sub_layers.put("token", tok_layer);
        }
        
        TextLayer dipl_layer = make_layer(tok_dipl_textlayer);
        dipl_layer.set_seg("tok_dipl");
        mod_layer = make_annotatable_layer(tok_mod_textlayer);
        mod_layer.set_seg("tok_mod");

        sub_layers.put("dipl", dipl_layer);
        sub_layers.put("mod", mod_layer);

        // initialize the timeline
        timeline = SaltFactory.createSTimeline();
        graph.setTimeline(timeline);
        // add initial PointOfTime
        timeline.increasePointOfTime();
    }

    public TextLayer layer(String coraxml_name) {
        return sub_layers.get(coraxml_name);
    }

    public void annotate(String name, Attributes attr) {
        this.annotate(name, attr.getValue("tag"));
    }
    public void annotate(String name, String value) {
        mod_layer.annotate(name, value);
    }

    public void annotate_boundary(String name, Attributes attr) {
        this.annotate_boundary(name, attr.getValue("tag"));
    }


    public void add_tok_to_span(String name, Attributes attr) {
        mod_layer.add_to_span(attr.getValue("span-id"));
        if (attr.getValue("tag") != null) {
            mod_layer.annotate_span(attr.getValue("span-id"), name, attr.getValue("tag"));
        }
    }

    /**
     * Creates a span annotation from the token after the last boundary
     * (or the first token) to the last added token.
     * @param name
     * @param value
     */
    public void annotate_boundary(String name, String value) {
        mod_layer.annotate_boundary(name, value);
    }

    public void map_tokens_to_timeline_simple() {
        Integer start = timeline.getEnd() - 1;

        // set initial start points and get maximal number of tokens
        int number_of_tokens = 0;
        for (TextLayer l : sub_layers.values()) {
            if (number_of_tokens < l.open_tokens.size()) {
                number_of_tokens = l.open_tokens.size();
            }
            l.set_start(start);
        }
        timeline.increasePointOfTime(number_of_tokens);

        // map all tokens except for the last
        for (TextLayer l : sub_layers.values()) {
            while (l.open_tokens.size() > 1) {
                l.tok_to_timeline(timeline, l.current_position + 1);
            }
        }

        // map the last tokens to the timeline (spanning all remaining points)
        Integer end = timeline.getEnd() - 1;
        for (TextLayer l : sub_layers.values())
            l.tok_to_timeline(timeline, end);
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
            if (tok_offset == layer("dipl").offsets.get(0)) {
                layer("dipl").tok_to_timeline(timeline, end);
                layer("dipl").offsets.remove(0);
            }
            if (tok_offset == layer("mod").offsets.get(0)) {
                layer("mod").tok_to_timeline(timeline, end);
                layer("mod").offsets.remove(0);
            }
        }
        if (layer("token") != null) {
        	layer("token").set_start(start);
            layer("token").tok_to_timeline(timeline,
                                      timeline.getEnd() - 1);
        }
    }
}

