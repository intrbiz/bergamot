package com.intrbiz.bergamot.ui.security.password.check;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A set of checks to apply to reduce poor password choices
 */
public class PasswordCheckEngine implements PasswordChecker
{
    private static final PasswordCheckEngine DEFAULT_INSTANCE = createDefaultInstance(8, 7);
    
    /**
     * Create a password checking engine using the default checkers
     * @param minLength the minimum password length to enforce
     */
    public static final PasswordCheckEngine createDefaultInstance(int minLength, int minUniqueCharacters)
    {
        PasswordCheckEngine engine = new PasswordCheckEngine();
        // enforce minimum length
        engine.registerChecker(new MinLengthChecker(minLength));
        // enforce mixed case
        engine.registerChecker(new MixedCaseChecker());
        // ban common passwords
        engine.registerChecker(new BannedPasswordChecker(new WordReader(PasswordCheckEngine.class.getResourceAsStream("common_passwords.txt"))));
        // enforce minimum number of unique characters
        engine.registerChecker(new UniqueCharactersChecker(minUniqueCharacters));
        return engine;
    }
    
    /**
     * Get the default, system wide password checking engine
     */
    public static final PasswordCheckEngine getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }
    
    private CopyOnWriteArrayList<PasswordChecker> checkers = new CopyOnWriteArrayList<PasswordChecker>();
    
    public PasswordCheckEngine()
    {
        super();
    }
    
    public void registerChecker(PasswordChecker checker)
    {
        this.checkers.add(checker);
    }
    
    @Override
    public void check(String password) throws BadPassword
    {
        for (PasswordChecker checker : this.checkers)
        {
            checker.check(password);
        }
    }
    
    public String toString()
    {
        return "Password Check Engine { checkers: " + this.checkers + " }";
    }
}
