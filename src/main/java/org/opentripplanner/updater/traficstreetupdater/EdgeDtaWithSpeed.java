package org.opentripplanner.updater.traficstreetupdater;

import org.opentripplanner.graph_builder.module.time.EdgeData;

public class EdgeDtaWithSpeed extends EdgeData {
    int speed ;

    public EdgeDtaWithSpeed(long id, int clusterid, long startnodeid, double startlatitude, double startlongitude, long endnodeid, double endlatitude, double endlongitude, long wayid, int speed) {
        super(id, clusterid, startnodeid, startlatitude, startlongitude, endnodeid, endlatitude, endlongitude, wayid);
   this.speed =speed;
    }


    public int getSpeed() {
        return speed;
    }


    public void setSpeed(int speed) {
        this.speed = speed;
    }
}
