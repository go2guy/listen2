package com.interact.listen.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Property
{
    public static enum Key {
        DNIS_MAPPING      ("com.interact.listen.dnisMapping", "770:Mailbox;990:Conferencing;*:Voicemail"),
        MAIL_SMTPHOST     ("com.interact.listen.mail.smtpHost", "localhost"),
        MAIL_SMTPUSERNAME ("com.interact.listen.mail.smtpUsername", ""),
        MAIL_SMTPPASSWORD ("com.interact.listen.mail.smtpPassword", ""),
        MAIL_FROMADDRESS  ("com.interact.listen.mail.fromAddress", "noreply@localhost"),
        PINLENGTH         ("com.interact.listen.pinLength", "10");

        private final String key;
        private final String defaultValue;

        private Key(String key, String defaultValue)
        {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public String getKey()
        {
            return key;
        }

        public String getDefaultValue()
        {
            return defaultValue;
        }

        public static Key findByKey(String search)
        {
            for(Key k : Key.values())
            {
                if(k.getKey().equals(search))
                {
                    return k;
                }
            }
            return null;
        }
    }
    
    @Column(nullable = false, unique = true)
    @Id
    private String key;

    @Column
    private String value;

    public static Property newInstance(String key, String value)
    {
        Property property = new Property();
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }
}
