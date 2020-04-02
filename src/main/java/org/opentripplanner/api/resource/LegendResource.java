package org.opentripplanner.api.resource;

import org.opentripplanner.analyst.core.Tile;
import org.opentripplanner.analyst.request.Renderer;
import org.opentripplanner.api.parameter.MIMEImageFormat;
import org.opentripplanner.api.parameter.Style;
import org.opentripplanner.api.parameter.StyleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.awt.image.BufferedImage;

@Path("/analyst/legend.{format}")
public class LegendResource {

    private static final Logger LOG = LoggerFactory.getLogger(LegendResource.class);

    @PathParam("format")
    String format;
    @QueryParam("width")
    @DefaultValue("300")
    int width;
    @QueryParam("height")
    @DefaultValue("150")
    int height;
    @QueryParam("styles")
    @DefaultValue("color30")
    StyleList styles;

    @GET
    @Produces("image/*")
    public Response tileGet() throws Exception {
        if (format.equals("jpg"))
            format = "jpeg";
        MIMEImageFormat mimeFormat = new MIMEImageFormat("image/" + format);
        Style style = styles.get(0);
        BufferedImage image = Tile.getLegend(style, width, height);
        return Renderer.generateStreamingImageResponse(image, mimeFormat);
    }

}