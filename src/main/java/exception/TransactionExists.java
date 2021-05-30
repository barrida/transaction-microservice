package exception;

import lombok.NoArgsConstructor;

/**
 * @author Suleyman Yildirim
 */

@NoArgsConstructor
public class TransactionExists extends Exception{
    public TransactionExists(String s){
        super(s);
    }
}
