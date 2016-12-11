package com.intrbiz.bergamot.model;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.junit.Test;

import com.intrbiz.bergamot.model.util.Parameter;

public class CheckCommandTest
{
    @Test
    public void testCheckCommandParameterResolution()
    {
        // the command
        Command command = new Command();
        command.addParameter("command_line", "check_dummy #{arg1} #{arg2}");
        command.addParameter("arg1", "#{host.address}");
        // the check
        CheckCommand checkCommand = new CheckCommand();
        checkCommand.addParameter("arg1", "127.0.0.1");
        checkCommand.addParameter("arg2", "TestValue!");
        // apply the parameter resolution
        LinkedHashMap<String, Parameter> resolved = checkCommand.resolveCheckParameters(command);
        assertThat(resolved, is(notNullValue()));
        assertThat(resolved, hasKey("arg1"));
        assertThat(resolved, hasKey("arg2"));
        assertThat(resolved, hasKey("command_line"));
        assertThat(resolved.entrySet().stream().map((e) -> e.getKey()).collect(Collectors.toList()).toArray(new String[0]), is(equalTo(new String[] {"command_line", "arg1", "arg2"})));
        assertThat(resolved.get("arg1").getValue(), is(equalTo("127.0.0.1")));
        assertThat(resolved.get("arg2").getValue(), is(equalTo("TestValue!")));
        assertThat(resolved.get("command_line").getValue(), is(equalTo("check_dummy #{arg1} #{arg2}")));
    }
}
