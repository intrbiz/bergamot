package com.intrbiz.bergamot.ui.security.password.check;

/**
 * Enforce that a password has a mix of upper and lower case
 */
public class MixedCaseChecker implements PasswordChecker
{       
    public MixedCaseChecker()
    {
        super();
    }
    
    public void check(String password) throws BadPassword
    {
        if (password.equals(password.toLowerCase()))
            throw new BadPassword("Not mixed case");
    }
    
    public String toString()
    {
        return "Mixed Case Checker";
    }
}
