package com.intrbiz.bergamot.ui.security.password.check;

import java.util.Set;
import java.util.TreeSet;

/**
 * 
 */
public class UniqueCharactersChecker implements PasswordChecker
{   
    private final int minUniqueCharacters;
    
    public UniqueCharactersChecker(int minUniqueCharacters)
    {
        super();
        this.minUniqueCharacters = minUniqueCharacters;
    }
    
    public void check(String password) throws BadPassword
    {
        Set<Character> unqChars = new TreeSet<Character>();
        for (char c : password.toCharArray())
        {
            unqChars.add(new Character(c));
        }
        if (unqChars.size() < this.minUniqueCharacters)
            throw new BadPassword("Not enough unique characters");
    }
    
    public String toString()
    {
        return "Unique Characters Checker (" + this.minUniqueCharacters + ")";
    }
}
