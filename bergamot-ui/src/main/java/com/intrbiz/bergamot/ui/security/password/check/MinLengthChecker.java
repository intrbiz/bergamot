package com.intrbiz.bergamot.ui.security.password.check;

/**
 * Enforce a minimum password length
 */
public class MinLengthChecker implements PasswordChecker
{
    private int minLength;
    
    public MinLengthChecker(int minLength)
    {
        this.minLength = minLength;
    }
    
    public void check(String password) throws BadPassword
    {
        if (password.length() < this.minLength)
            throw new BadPassword("Too short");
    }
    
    public String toString()
    {
        return "Min Length Checker (" + this.minLength + ")";
    }
}
