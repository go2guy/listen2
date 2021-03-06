package com.interact.listen.attendant;

import com.interact.listen.command.IvrCommand;

import java.util.List;

import javax.persistence.*;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Entity
@Table(name = "attendant_menu")
public class Menu implements IvrCommand
{
    public static final String TOP_MENU_NAME = "Top Menu"; // TODO fix hard-coded; perhaps an 'isPermanent' field

    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(name = "VERSION")
    @Version
    private Integer version = Integer.valueOf(0);

    @Column(name = "NAME", nullable = false, unique = true)
    private String name;

    @Column(name = "AUDIO_FILE")
    private String audioFile;

    @JoinColumn(name = "DEFAULT_ACTION_ID")
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private Action defaultAction;

    @JoinColumn(name = "TIMEOUT_ACTION_ID")
    @OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE })
    private Action timeoutAction;

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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAudioFile()
    {
        return audioFile;
    }

    public void setAudioFile(String audioFile)
    {
        this.audioFile = audioFile;
    }

    public Action getDefaultAction()
    {
        return defaultAction;
    }

    public void setDefaultAction(Action defaultAction)
    {
        this.defaultAction = defaultAction;
    }

    public Action getTimeoutAction()
    {
        return timeoutAction;
    }

    public void setTimeoutAction(Action timeoutAction)
    {
        this.timeoutAction = timeoutAction;
    }

    public JSONObject toJson(Session session)
    {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("audioFile", audioFile);
        json.put("defaultAction", defaultAction.toJson());
        json.put("timeoutAction", timeoutAction.toJson());

        JSONArray actions = new JSONArray();
        for(Action action : Action.queryByMenuWithoutDefaultAndTimeout(session, this))
        {
            actions.add(action.toJson());
        }

        json.put("actions", actions);
        return json;
    }
    
    public String toIvrCommandJson(Session session)
    {
        JSONObject returnObject = new JSONObject();
        returnObject.put("action", "PROMPT");
        
        JSONObject args = new JSONObject();
        List<Action> targetMenuActions = Action.queryByMenuWithoutDefaultAndTimeout(session, this);
        JSONArray keyPresses = new JSONArray();
        for(Action action : targetMenuActions)
        {
            keyPresses.add(action.getKeyPressed());
        }
        
        args.put("id", id);
        args.put("keyPresses", keyPresses);
        args.put("audioFile", audioFile);
        
        returnObject.put("args", args);
        
        return returnObject.toString();
    }
    
    public static List<Menu> queryAll(Session session)
    {
        Criteria criteria = session.createCriteria(Menu.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return (List<Menu>)criteria.list();
    }
    
    public static Menu queryById(Session session, Long id)
    {
        return (Menu)session.get(Menu.class, id);
    }
    
    public static Menu queryTopMenu(Session session)
    {
        Criteria criteria = session.createCriteria(Menu.class);
        criteria.add(Restrictions.eq("name", TOP_MENU_NAME));
        criteria.setMaxResults(1);
        return (Menu)criteria.uniqueResult();
    }

    public static List<Menu> queryByName(Session session, String name)
    {
        Criteria criteria = session.createCriteria(Menu.class);
        criteria.add(Restrictions.eq("name", name));
        return (List<Menu>)criteria.list();
    }
}
