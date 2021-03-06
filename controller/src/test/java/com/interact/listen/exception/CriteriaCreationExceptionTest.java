package com.interact.listen.exception;

import static org.junit.Assert.assertEquals;

import com.interact.listen.exception.CriteriaCreationException;

import org.junit.Test;

public class CriteriaCreationExceptionTest
{
    @Test
    public void test_construct_setsMessage()
    {
        final String message = "Twas brillig and the slithy toves did gyre and gimble in the wabe";
        Exception e = new CriteriaCreationException(message);
        assertEquals(message, e.getMessage());
    }
}
