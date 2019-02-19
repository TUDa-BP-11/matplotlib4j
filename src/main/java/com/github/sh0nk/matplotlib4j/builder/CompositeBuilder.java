package com.github.sh0nk.matplotlib4j.builder;

import com.github.sh0nk.matplotlib4j.TypeConversion;
import static java.util.stream.Collectors.joining;
//import com.google.common.base.Joiner;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link CompositeBuilder} handles positional arguments and keyword arguments
 * to methods by {@link #build()} call on behalf of the ownerBuilder with a
 * common way.
 *
 * @param <T> Owner builder class
 */
public class CompositeBuilder<T extends Builder> implements Builder {

//    private final static Logger LOGGER = LoggerFactory.getLogger(CompositeBuilder.class);
    private final List<Object> args = new LinkedList<>();
    private final Map<String, Object> kwargs = new HashMap<>();
    private String beforeMethodOutput = null;
    private String afterMethodOutput = null;
    private String afterMethodOutputDate = null;

    private final T ownerBuilder;

    // get unique return value
    private final String retName = "ret_" + UUID.randomUUID().toString().replace('-', '_');

    public CompositeBuilder(T ownerBuilder) {
        this.ownerBuilder = ownerBuilder;
    }

    private String wrapWithNdArray(String listAsStr) {
        // Change all the array_like arguments from python list to np.array because .shape is called in pcolor
        return "np.array(" + listAsStr + ")";
    }

    public T addToArgs(List<? extends Number> numbers) {
        args.add(wrapWithNdArray(TypeConversion.INSTANCE.typeSafeList(numbers).toString()));
        return ownerBuilder;
    }

    public T addDateListToArgs(List<? extends Number> numbers) {
        List<Object> numbersNoNull = TypeConversion.INSTANCE.typeSafeList(numbers);
        afterMethodOutputDate = "import matplotlib.dates as mdates\n"
//                + "days = mdates.DayLocator()\n"
//                + "months = mdates.MonthLocator()\n"
//                + "locator = mdates.AutoDateLocator()\n"
//                + "fmt = mdates.AutoDateFormatter(locator)\n"
                + "fmt = mdates.DateFormatter('%d.%m.%Y %H:%M')\n"
                + "ax = plt.gca()\n"
//                + "ax.xaxis.set_major_locator(months)\n"
                + "ax.xaxis.set_major_formatter(fmt)\n"
//                + "ax.xaxis.set_minor_locator(days) \n"
//                + "ax.format_xdata = fmt\n"
                + "ax.grid(True)\n";
//                + "plt.gcf().autofmt_xdate()";
//                "locator = mpl.dates.AutoDateLocator() \nformatter = mpl.dates.AutoDateFormatter(locator)"

        String str = wrapWithNdArray("mpl.dates.epoch2num(" + numbersNoNull.toString() + ")");
        args.add(str);
//        System.out.println(str);
//        System.out.println(afterMethodOutputDate);
        return ownerBuilder;
    }

    public T add2DimListToArgs(List<? extends List<? extends Number>> numbers) {
        args.add(wrapWithNdArray(
                numbers.stream().map(TypeConversion.INSTANCE::typeSafeList).collect(Collectors.toList()).toString()
        ));
        return ownerBuilder;
    }

    public T addToArgs(String v) {
        // TODO: Do it with StringBuilder on join
        args.add("\"" + v + "\"");
        return ownerBuilder;
    }

    public T addToArgsWithoutQuoting(String v) {
        args.add(v);
        return ownerBuilder;
    }

    public T addToArgs(Number n) {
        args.add(n);
        return ownerBuilder;
    }

    public T addToKwargs(String k, String v) {
        // TODO: Do it with StringBuilder on join
        kwargs.put(k, "\"" + v + "\"");
        return ownerBuilder;
    }

    public T addToKwargsWithoutQuoting(String k, String v) {
        kwargs.put(k, v);
        return ownerBuilder;
    }

    public T addToKwargs(String k, Number n) {
        kwargs.put(k, n);
        return ownerBuilder;
    }

    public T addToKwargs(String k, List<? extends Number> v) {
        kwargs.put(k, v);
        return ownerBuilder;
    }

    public T addToKwargs(String k, boolean v) {
        kwargs.put(k, v ? "True" : "False");
        return ownerBuilder;
    }

    public void beforeMethodOutput(String arg) {
        beforeMethodOutput = arg;
    }

    public void afterMethodOutput(String arg) {
        afterMethodOutput = arg;
    }

    @Override
    public String build() {
        StringBuilder sb = new StringBuilder();
        if (beforeMethodOutput != null) {
            sb.append(beforeMethodOutput).append('\n');
        }

        // retName
        sb.append(retName).append(" = ");

        sb.append("plt.");
        sb.append(ownerBuilder.getMethodName());
        sb.append("(");

        // Args
        // TODO: type conversion
//        Joiner.on(',').appendTo(sb, args);
        sb.append(args.stream().map(Object::toString).collect(joining(",")));
        // Kwargs
        if (!kwargs.isEmpty()) {
            if (!args.isEmpty()) {
                sb.append(',');
            }
//            Joiner.on(',').withKeyValueSeparator("=").appendTo(sb, kwargs);
            sb.append(kwargs.entrySet()
                    .stream()
                    .map(Object::toString)
                    .collect(joining(",")));
        }

        sb.append(")");

        if (afterMethodOutput != null) {
            sb.append('\n').append(afterMethodOutput);
        }

        if (afterMethodOutputDate != null) {
            sb.append('\n').append(afterMethodOutputDate);
        }

        String str = sb.toString();
//        System.out.println("CompositeBuilder.build(): "+str);
//        LOGGER.debug(".plot command: {}", str);
        return str;
    }

    @Override
    public String getMethodName() {
        throw new UnsupportedOperationException("CompositeBuilder doesn't have any real method.");
    }

    public String getRetName() {
        return retName;
    }

}
