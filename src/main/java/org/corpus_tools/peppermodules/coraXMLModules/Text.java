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
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        private SToken add_text(String value) {
            int left_pos = textual.getText().length();
            value = StringEscapeUtils.unescapeHtml4(value);
            textual.setText(textual.getText() + value);
            int right_pos = left_pos + value.length();
            return graph.createToken(textual, left_pos, right_pos);
        }

        public TextLayer add_token(String value, Integer len) {
            SToken tok = this.add_text(value);
            order(last_added, tok);
            if (open_tokens != null && offsets != null) {
                open_tokens.add(tok);
                offsets.add(last_offset() + len);
            }
            last_added = tok;
            textual.setText(textual.getText() + " ");

            return this;
        }
        public TextLayer add_token(Attributes attr) {
            return this.add_token(attr.getValue(layer_name), attr.getValue("trans").length());
        }

        public TextLayer add_comment(String text, String type) {
            this.add_token(text, 1);
            last_token().createAnnotation("annotation", "editorial_comment", type);
            return this;
        }

        class Subtokenspan {
            private int start;
            private int end;
            private SSpan span;

            Subtokenspan(int start, int end, String type, String text) {
                this.setStart(start);
                this.setEnd(end);
                this.span = SaltFactory.createSSpan();
                graph.addNode(getSpan());
                getSpan().createAnnotation("annotation", type, text);
            }

      public int getStart() {
        return start;
      }

      public void setStart(int start) {
        this.start = start;
      }

      public int getEnd() {
        return end;
      }

      public void setEnd(int end) {
        this.end = end;
      }

      public SSpan getSpan() {
        return span;
      }

        }

        abstract class Subtokenizer {
            Map<String, String> markup_types = new HashMap<String, String>();

            public abstract String remove_special(String text);

            public void add_subtoken_spans(String transcription, Set<Integer> splitpoints, List<Subtokenspan> spans) {
            for(Map.Entry<String, String> type: markup_types.entrySet()) {
              add_subtoken_span(transcription, type.getValue(), type.getKey(), splitpoints, spans);
            }
          }

          private void add_subtoken_span(String transcription, String regex, String type,
              Set<Integer> splitpoints, List<Subtokenspan> spans) {
              Matcher m = Pattern.compile(regex).matcher(transcription);
              while(m.find()) {
                splitpoints.add(m.start(0));
                splitpoints.add(m.end(0));

                spans.add(new Subtokenspan(m.start(0), m.end(0), type, this.remove_special(m.group(0))));
              }
          }
      }

      class RENSubtokenizer extends Subtokenizer {

        public RENSubtokenizer() {
            markup_types.put("unreadable", "\\[[^\\]]+\\]");
            markup_types.put("deleted", "ǂ[^ǂ]+ǂ");
            markup_types.put("expanded", "\\{._[^}]+\\}");
            markup_types.put("para", "\\*[ILROUT][NKE]_[^*]+\\*");
        }

        @Override
          public String remove_special(String text) {
            return text.replaceAll("[\\[\\]}ǂ#§]", "")
                .replaceAll("\\{._", "")
                .replaceAll("\\*[ILROUT][NKE]_", "").replaceAll("\\*", "");
          }


      }

      private Subtokenizer subtokenizerFactory(String style) {
        if (style.equals("REN")) {
          return new RENSubtokenizer();
        }
        return null;
      }

        public TextLayer add_token(Attributes attr, String subtok_ann_style) {

        Subtokenizer subtok = subtokenizerFactory(subtok_ann_style);
        if (subtok==null) {
          throw new RuntimeException("Transcription-style " + subtok_ann_style + " is not implemented.");
        }

        // get positions for splits and create annoation spans
        String transcription = attr.getValue("trans");
        Set<Integer> splitpoints = new TreeSet<Integer>();
        List<Subtokenspan> spans = new LinkedList<Subtokenspan>();

        subtok.add_subtoken_spans(transcription, splitpoints, spans);

        splitpoints.add(transcription.length());
        splitpoints.remove(0);

        // split string into subtokens
        List<String> parts = new LinkedList<String>();
        Integer last_split = 0;
        for (Integer split: splitpoints) {
          // join parts that are only contain markup with previous text
          if(!subtok.remove_special(transcription.substring(last_split, split)).isEmpty()) {
            parts.add(transcription.substring(last_split, split));
            last_split = split;
          }
        }
        // add the end of the token to the last part, if it only contains markup
        if(last_split < transcription.length()) {
          parts.set(parts.size()-1, parts.get(parts.size()-1) +
              transcription.substring(last_split, transcription.length()));
        }

        // add subtokens to corresponding spans
        int position = 0;
        for (int i=0; i < parts.size(); i++) {

          int len = parts.get(i).length();
          String text = subtok.remove_special(parts.get(i));
          this.add_token(text, len);

          // find the position of the start of the text
          int pos0 = position;
          position = position + parts.get(i).indexOf(text);

          for (Subtokenspan span: spans) {
            if (position >=  span.getStart() && position < span.getEnd()) {
              SSpanningRelation rel = SaltFactory.createSSpanningRelation();
              rel.setSource(span.getSpan());
              rel.setTarget(this.last_token());
              graph.addRelation(rel);
            }
          }

          position = pos0 + len;

        }

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

        private Map<String, List<SToken>> open_boundary_tokens = new HashMap<String, List<SToken>>();
        private Map<String, SSpan> span_annotations = new HashMap<String, SSpan>();

        protected ModLayer(String l, SDocumentGraph my_graph, Set<String> boundary_types) {
            super(l, my_graph);
            for (String b_type: boundary_types) {
                open_boundary_tokens.put(b_type, new LinkedList<SToken>());
            }
        }

        @Override
        public ModLayer add_token(Attributes attr) {

            super.add_token(attr);
            for(String b_type: open_boundary_tokens.keySet()) {
                open_boundary_tokens.get(b_type).add(this.last_token());
            }

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

            List<SToken> boundary_tokens = open_boundary_tokens.get(name);

            if (boundary_tokens != null) {

                // ignore boundaries with value "None"
                if(!value.equals("None")) {
                    SSpan span = SaltFactory.createSSpan();
                    span.createAnnotation("annotation", name, value);
                    graph.addNode(span);

                    for (SToken tok: boundary_tokens) {
                        SSpanningRelation rel = SaltFactory.createSSpanningRelation();
                        rel.setSource(span);
                        rel.setTarget(tok);
                        graph.addRelation(rel);
                    }
                }
                boundary_tokens.clear();
            }

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
    private ModLayer make_annotatable_layer(String layer_name, Set<String> boundary_types) {
        return new ModLayer(layer_name, graph, boundary_types);
    }

    private Text() {}
    public Text(SDocumentGraph the_graph,
                String tok_dipl_textlayer, String tok_mod_textlayer,
                boolean export_token_layer, boolean export_subtoken_annotation, Set<String> boundary_types) {

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
        mod_layer = make_annotatable_layer(tok_mod_textlayer, boundary_types);
        mod_layer.set_seg("tok_anno");

        sub_layers.put("tok_dipl", dipl_layer);
        sub_layers.put("tok_anno", mod_layer);

        if (export_subtoken_annotation) {
          TextLayer subtok_layer = make_layer("trans");
          subtok_layer.set_seg("tok_sub");
            sub_layers.put("sub", subtok_layer);
        }


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

      int start = timeline.getEnd() - 1;

        SortedSet<Integer> tok_offsets = new TreeSet<Integer>();
        for(String layer: sub_layers.keySet()) {
          layer(layer).set_start(start);
          if (!layer.equals("token")) {
            tok_offsets.addAll(layer(layer).offsets);
          }
        }

        for (Integer tok_offset : tok_offsets) {
            timeline.increasePointOfTime();
            int end = timeline.getEnd() - 1;
            for(String layer: sub_layers.keySet()) {
              if ((!layer.equals("token")) && (tok_offset == layer(layer).offsets.get(0))) {
                layer(layer).tok_to_timeline(timeline, end);
                layer(layer).offsets.remove(0);
              }
            }
        }
        if (layer("token") != null) {
            if(start == timeline.getEnd()-1) {
                timeline.increasePointOfTime();
            }
            layer("token").tok_to_timeline(timeline,
                                      timeline.getEnd() - 1);
        }
    }
}
