package com.intrbiz.bergamot.ui.security.password.check;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * Check the given password against a set of know bad passwords
 */
public class BannedPasswordChecker implements PasswordChecker
{
    private final Set<String> bannedPasswords = new TreeSet<String>();
    
    public BannedPasswordChecker(WordReader source)
    {
        super();
        String word;
        try
        {
            while ((word = source.next()) != null)
            {
                this.bannedPasswords.add(word.toLowerCase());
            }
        }
        catch (IOException e)
        {
            Logger.getLogger(BannedPasswordChecker.class).warn("Failed to load list of banned passwords", e);
        }
        finally
        {
            source.close();
        }
    }
    
    public BannedPasswordChecker(Set<String> passwords)
    {
        super();
        for (String word : passwords)
        {
            this.bannedPasswords.add(word.toLowerCase());
        }
    }
    
    @Override
    public void check(String password) throws BadPassword
    {
        if (this.bannedPasswords.contains(password.toLowerCase()))
            throw new BadPassword("Banned password");
    }
    
    public String toString()
    {
        return "Banned Password Checker";
    }
}
