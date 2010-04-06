package com.interact.listen.resource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.CollectionOfElements;

@Entity
public class Subscriber extends Resource implements Serializable
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Version
    private Integer version = Integer.valueOf(0);

    @Column(unique = true, nullable = false)
    private String number;
    
    private String voicemailGreetingLocation;

    @CollectionOfElements
    private List<Voicemail> voicemails = new ArrayList<Voicemail>();

    public String getNumber()
    {
        return number;
    }

    public void setNumber(String number)
    {
        this.number = number;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }
    
    public String getVoicemailGreetingLocation()
    {
        return voicemailGreetingLocation;
    }

    public void setVoicemailGreetingLocation(String location)
    {
        this.voicemailGreetingLocation = location;
    }

    public List<Voicemail> getVoicemails()
    {
        return voicemails;
    }

    public void setVoicemails(List<Voicemail> voicemails)
    {
        this.voicemails = voicemails;
    }

    @Override
    public void validate()
    {
        if(number == null || number.trim().equals(""))
        {
            addToErrors("Subscriber must have a number");
        }
    }
}
