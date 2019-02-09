package com.github.sh0nk.matplotlib4j.builder;

import com.github.sh0nk.matplotlib4j.kwargs.Line2DBuilder;

import java.util.List;
import java.util.Date;


/**
 * matplotlib.pyplot.plot(*args, **kwargs)
 */
public interface PlotBuilder extends Builder, Line2DBuilder<PlotBuilder> {

    PlotBuilder add(List<? extends Number> nums);
    
    PlotBuilder addDates(List<? extends Number> dates);

    PlotBuilder add(List<? extends Number> x, List<? extends Number> y);

    PlotBuilder add(List<? extends Number> x, List<? extends Number> y, String fmt);
   
    PlotBuilder color(String arg);
    
    PlotBuilder marker(String arg);
    
    PlotBuilder markersize(String arg);
}
