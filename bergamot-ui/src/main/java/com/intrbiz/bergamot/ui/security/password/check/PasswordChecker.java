package com.intrbiz.bergamot.ui.security.password.check;

public interface PasswordChecker
{
    void check(String password) throws BadPassword;
}
