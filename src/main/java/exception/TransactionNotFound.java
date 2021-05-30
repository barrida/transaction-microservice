package exception;

import lombok.NoArgsConstructor;

/**
 * @author Suleyman Yildirim
 */
@NoArgsConstructor
public class TransactionNotFound extends Exception{
    public TransactionNotFound(String s){
        super(s);
    }
}
