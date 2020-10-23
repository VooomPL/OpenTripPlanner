package org.opentripplanner.updater.traficstreetupdater;

import org.opentripplanner.hasura_client.hasura_objects.HasuraObject;

public class EdgeDataWithSpeed extends HasuraObject {
    private long id;

    private long startnodeid;
    private  long endnodeid;
    private int speed;

    public long getEndnodeid() {
        return endnodeid;
    }

    public void setEndnodeid(long endnodeid) {
        this.endnodeid = endnodeid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartnodeid() {
        return startnodeid;
    }

    public void setStartnodeid(long startnodeid) {
        this.startnodeid = startnodeid;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }


}
