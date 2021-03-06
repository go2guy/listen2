package com.interact.listen

import grails.test.TagLibUnitTestCase
import org.joda.time.DateTime

class CopyrightTagLibTests extends TagLibUnitTestCase {
    CopyrightTagLibTests() {
        super(CopyrightTagLib)
    }

    // outputs copyright
    void testCopyright0() {
        final def thisYear = new DateTime().year
        tagLib.copyright()
        assertEquals "Listen &copy;2012-${thisYear} NewNet Communication Technologies, <a href='http://www.newnet.com' title='NewNet Communication Technologies'>newnet.com</a>", tagLib.out.toString()
    }
}
