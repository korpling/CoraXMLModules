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

import java.util.Map;
import java.util.Hashtable;

import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.xml.sax.Attributes;

class Layout {
    private SDocumentGraph graph;
    private Map<String, LayoutElement> element_starts
        = new Hashtable<String, LayoutElement>();
    private LayoutElement last_page;
    private char current_column = 'a';
    private LayoutElement current_element;

    private Layout() {}
    public Layout(SDocumentGraph the_graph) {
        graph = the_graph;
    }

    private void update(String id, LayoutElement elem) {
        if (element_starts.get(id) != null) {
            current_element = element_starts.get(id);
            element_starts.remove(current_element);
        }
        elem.set_connection(current_element);
    }

    private void add(LayoutElement element) {
        element_starts.put(element.from(), element);
    }

    private LayoutElement make_element(int level, String annis_name) {
        return new LayoutElement(level, annis_name, graph);
    }
    public Layout make_page(Attributes attr) {
        LayoutElement page
            = make_element(0, "page").set_range(attr)
                                          .set_value(attr.getValue("no"));
        if (last_page == null || !last_page.value.equals(page.value)) {
            add(page);
            last_page = page;
        } else {
            last_page.to = page.to();
        }
        return this;
    }
    public Layout make_side(Attributes attr) {
        if (attr.getValue("side") == null || attr.getValue("side").isEmpty())
            return this;

        LayoutElement side
            = make_element(1, "side").set_range(attr)
                                          .set_value(attr.getValue("side"));
        update(side.from, side);
        add(side);
        return this;
    }
    public String make_column(Attributes attr) {

        if (element_starts.get(attr.getValue("id")) != null) {
            current_column = 'a';
        }

        String col_name;
        if (attr.getValue("name") != null) {
            col_name = attr.getValue("name");
        }
        else {
            col_name = Character.toString(current_column);
        }
        current_column++;

        LayoutElement column
            = make_element(2, "column").set_range(attr)
                                       .set_value(col_name);
        update(attr.getValue("id"), column);

        add(column);
        return col_name;
    }
    public Layout make_line(Attributes attr) {
        LayoutElement line = make_element(3, "line").set_range(attr)
                                                    .set_value(attr.getValue("name"));
        update(attr.getValue("id"), line);
        add(line);

        if (attr.getValue("loc") != null) {
        	LayoutElement loc = make_element(4, "reference").set_range(attr)
                                                            .set_value(attr.getValue("loc"));
        	update(loc.from(), loc);
        	add(loc);
        }
        
        return this;
    }
    public Layout render(String id, SToken tok) {
        if (element_starts.get(id) != null)
            current_element = element_starts.get(id);
        current_element.render(tok);
        return this;
    }
    class LayoutElement {
        private int level;
        private String name;
        private SDocumentGraph graph;
        private SSpan span;

        protected String from, to;
        protected LayoutElement parent_from;
        private String namespace = "layout";
        public String value;

        public String toString() {
            return "{" + name + "(" + from + ".." + to + ")"
                + " (parent:" + parent_from + ")"
                + "}";
        }

        protected LayoutElement() {}
        protected LayoutElement(int my_level, String annis_name, SDocumentGraph the_graph) {
            level = my_level;
            name = annis_name;
            graph = the_graph;
        }
        public LayoutElement set_range(Attributes attr) {
            String[] rangeparts = attr.getValue("range")
                                      .split("[.][.]");
            from = rangeparts[0];
            to = rangeparts.length == 1 ? rangeparts[0] : rangeparts[1];
            return this;
        }
        public String from() { return from; }
        public String to() { return to; }
        public LayoutElement set_connection(LayoutElement elem) {
            if (name == elem.name) { // case: we have a sibling where we can take the other's parent
                parent_from = elem.parent_from;
                return this;
            }
            if (level > elem.level) //  case: we have a parent (low level is up)
                parent_from = elem;
            if (level < elem.level) // case: we connect on a child where we have to take the parent's parent
                parent_from = elem.parent_from.parent_from;
            return this;
        }
        public LayoutElement set_value(String val) {
            value = val;
            return this;
        }
        private void span_on_tok(SToken tok) {
            SSpanningRelation rel = SaltFactory.createSSpanningRelation();
            rel.setSource(get_span());
            rel.setTarget(tok);
            graph.addRelation(rel);
        }
        public void render(SToken tok) {
            span_on_tok(tok);
            if (parent_from != null)
                parent_from.render(tok);
        }
        private SSpan get_span() {
            // the span can't be created in the ctor because
            // we don't know the value yet there
            if (span == null) {
                span = SaltFactory.createSSpan();
                span.createAnnotation(namespace, name, value);
                graph.addNode(span);
            }
            return span;
        }
    }
}
