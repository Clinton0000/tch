/*
 * Copyright (c) [2016] [ <tch.camp> ]
 * This file is part of the tcheumJ library.
 *
 * The tcheumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The tcheumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the tcheumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tcheum.json;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.SegmentedStringWriter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * An extended {@link com.fasterxml.jackson.databind.ObjectMapper ObjectMapper} class to
 * customize tcheum state dumps.
 *
 * @author Alon Muroch
 */
public class tchObjectMapper extends ObjectMapper {

    @Override
    public String writeValueAsString(Object value)
            throws JsonProcessingException {
        // alas, we have to pull the recycler directly here...
        SegmentedStringWriter sw = new SegmentedStringWriter(_jsonFactory._getBufferRecycler());
        try {
            JsonGenerator ge = _jsonFactory.createGenerator(sw);
            // set tcheum custom pretty printer
            tchPrettyPrinter pp = new tchPrettyPrinter();
            ge.setPrettyPrinter(pp);

            _configAndWriteValue(ge, value);
        } catch (JsonProcessingException e) { // to support [JACKSON-758]
            throw e;
        } catch (IOException e) { // shouldn't really happen, but is declared as possibility so:
            throw JsonMappingException.fromUnexpectedIOE(e);
        }
        return sw.getAndClear();
    }

    /**
     * An extended {@link com.fasterxml.jackson.core.util.DefaultPrettyPrinter} class to customize
     * an tcheum {@link com.fasterxml.jackson.core.PrettyPrinter Pretty Printer} Generator
     *
     * @author Alon Muroch
     */
    public class tchPrettyPrinter extends DefaultPrettyPrinter {

        public tchPrettyPrinter() {
            super();
        }

        @Override
        public void writeObjectFieldValueSeparator(JsonGenerator jg)
                throws IOException, JsonGenerationException {
            /**
             * Custom object separator (Default is " : ") to make it easier to compare state dumps with other
             * tcheum client implementations
             */
            jg.writeRaw(": ");
        }
    }
}
