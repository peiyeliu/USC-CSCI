// Name: Peiye Liu
// USC NetID: peiyeliu
// CS 455 PA4
// Fall 2020

import java.io.IOException;

/**
 * This class reports problems with the dictionary file.
 */
public class IllegalDictionaryException extends IOException {
    public IllegalDictionaryException() {
    }

    public IllegalDictionaryException(String message) {
        super(message);
    }
}
