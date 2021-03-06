/*
    Copywrite 2016 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.gcode.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.processors.*;
import com.willwinder.universalgcodesender.utils.Settings;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class CommandProcessorLoaderTest {

    @Test
    public void testInvalidProcessors() throws Exception {
        System.out.println("InvalidProcessor");
        GcodeParser gcp = new GcodeParser();

        JsonObject args = new JsonObject();
        JsonObject object = new JsonObject();
        object.addProperty("name", "DoesNotExist");
        object.add("args", args);

        JsonArray array = new JsonArray();
        array.add(object);

        boolean threwException = false;
        try {
            List<ICommandProcessor> result = CommandProcessorLoader.initializeWithProcessors(array.toString());
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);

        array = new JsonArray();
        array.add(with("DoesNotExist", true));
        threwException = false;
        try {
            List<ICommandProcessor> result = CommandProcessorLoader.initializeWithProcessors(array.toString(), new Settings());
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);
    }

    @Test
    public void testInvalidParametersToValidProcessor() throws Exception {
        System.out.println("InvalidProcessor");
        GcodeParser gcp = new GcodeParser();

        JsonObject args = new JsonObject();
        args.addProperty("length", "NotANumber");
        JsonObject object = new JsonObject();
        object.addProperty("name", "ArcExpander");
        object.add("args", args);

        JsonArray array = new JsonArray();
        array.add(object);

        boolean threwException = false;
        try {
            List<ICommandProcessor> result = CommandProcessorLoader.initializeWithProcessors(array.toString());
        } catch (IllegalArgumentException e) {
            threwException = true;
        }
        assertTrue(threwException);
    }

    /**
     * Test of initializeWithProcessors method, of class CommandProcessorLoader.
     */
    @Test
    public void testAllProcessors() throws Exception {
        System.out.println("initializeWithProcessors");
        JsonObject args, name, object;

        JsonArray array = new JsonArray();

        args = new JsonObject();
        args.addProperty("length", 1.5);
        object = new JsonObject();
        object.addProperty("name", "ArcExpander");
        object.add("args", args);
        array.add(object);

        object = new JsonObject();
        object.addProperty("name", "CommandSplitter");
        array.add(object);

        object = new JsonObject();
        object.addProperty("name", "CommentProcessor");
        object.add("args", null);
        array.add(object);

        args = new JsonObject();
        args.addProperty("decimals", 4);
        object = new JsonObject();
        object.addProperty("name", "DecimalProcessor");
        object.add("args", args);
        array.add(object);

        args = new JsonObject();
        args.addProperty("speed", 4);
        object = new JsonObject();
        object.addProperty("name", "FeedOverrideProcessor");
        object.add("args", args);
        array.add(object);

        object = new JsonObject();
        object.addProperty("name", "M30Processor");
        object.add("args", null);
        array.add(object);

        args = new JsonObject();
        args.addProperty("pattern", "1234");
        object = new JsonObject();
        object.addProperty("name", "PatternRemover");
        object.add("args", args);
        array.add(object);

        args = new JsonObject();
        args.addProperty("commandLength", 4);
        object = new JsonObject();
        object.addProperty("name", "CommandLengthProcessor");
        object.add("args", args);
        array.add(object);

        object = new JsonObject();
        object.addProperty("name", "WhitespaceProcessor");
        object.add("args", args);
        array.add(object);

        String jsonConfig = array.toString();
        List<ICommandProcessor> processors = CommandProcessorLoader.initializeWithProcessors(jsonConfig);

        assertEquals(9, processors.size());
        assertEquals(ArcExpander.class, processors.get(0).getClass());
        assertEquals(CommandSplitter.class, processors.get(1).getClass());
        assertEquals(CommentProcessor.class, processors.get(2).getClass());
        assertEquals(DecimalProcessor.class, processors.get(3).getClass());
        assertEquals(FeedOverrideProcessor.class, processors.get(4).getClass());
        assertEquals(M30Processor.class, processors.get(5).getClass());
        assertEquals(PatternRemover.class, processors.get(6).getClass());
        assertEquals(CommandLengthProcessor.class, processors.get(7).getClass());
        assertEquals(WhitespaceProcessor.class, processors.get(8).getClass());
    }
    
    private static JsonElement with(String name, Boolean enabled) {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("enabled", enabled);
        return object;
    }

    private static JsonElement with(String name, Boolean enabled, Boolean optional) {
        JsonObject object = new JsonObject();
        object.addProperty("name", name);
        object.addProperty("enabled", enabled);
        object.addProperty("optional", optional);
        return object;
    }

    @Test
    public void testAllProcessorsSettingConfig() throws Exception {
        System.out.println("initializeWithProcessorsSettingConfig");

        JsonArray object = new JsonArray();

        object.add(with("ArcExpander", true));
        object.add(with("ArcExpander", true));
        object.add(with("ArcExpander", false));
        object.add(with("CommandSplitter", true));
        object.add(with("CommentProcessor", true));
        object.add(with("DecimalProcessor", true));
        object.add(with("FeedOverrideProcessor", true));
        object.add(with("M30Processor", true));
        //object.add(with("PatternRemover", true));
        object.add(with("CommandLengthProcessor", true));
        object.add(with("WhitespaceProcessor", true));

        String jsonConfig = object.toString();

        List<ICommandProcessor> processors = CommandProcessorLoader.initializeWithProcessors(jsonConfig, new Settings());

        assertEquals(9, processors.size());
        assertEquals(ArcExpander.class, processors.get(0).getClass());
        assertEquals(ArcExpander.class, processors.get(1).getClass());
        assertEquals(CommandSplitter.class, processors.get(2).getClass());
        assertEquals(CommentProcessor.class, processors.get(3).getClass());
        assertEquals(DecimalProcessor.class, processors.get(4).getClass());
        assertEquals(FeedOverrideProcessor.class, processors.get(5).getClass());
        assertEquals(M30Processor.class, processors.get(6).getClass());
        assertEquals(CommandLengthProcessor.class, processors.get(7).getClass());
        assertEquals(WhitespaceProcessor.class, processors.get(8).getClass());
    }

    @Test
    public void testAllProcessorsSettingConfigOptionals() throws Exception {
        System.out.println("initializeWithProcessorsSettingConfigOptionals");

        JsonArray object = new JsonArray();

        object.add(with("ArcExpander", true, true));
        object.add(with("ArcExpander", true, true));
        object.add(with("ArcExpander", false, false));
        object.add(with("CommandSplitter", false, true));
        object.add(with("CommentProcessor", false, false));
        object.add(with("DecimalProcessor", true, false));
        object.add(with("FeedOverrideProcessor", true, false));
        object.add(with("M30Processor", true));
        object.add(with("CommandLengthProcessor", true));
        JsonObject o = with("WhitespaceProcessor", true).getAsJsonObject();
        o.remove("enabled");
        object.add(o);

        String jsonConfig = object.toString();

        List<ICommandProcessor> processors = CommandProcessorLoader.initializeWithProcessors(jsonConfig, new Settings());

        assertEquals(9, processors.size());
        assertEquals(ArcExpander.class, processors.get(0).getClass());
        assertEquals(ArcExpander.class, processors.get(1).getClass());
        assertEquals(ArcExpander.class, processors.get(1).getClass());
        assertEquals(CommentProcessor.class, processors.get(3).getClass());
        assertEquals(DecimalProcessor.class, processors.get(4).getClass());
        assertEquals(FeedOverrideProcessor.class, processors.get(5).getClass());
        assertEquals(M30Processor.class, processors.get(6).getClass());
        assertEquals(CommandLengthProcessor.class, processors.get(7).getClass());
        assertEquals(WhitespaceProcessor.class, processors.get(8).getClass());
    }
}
