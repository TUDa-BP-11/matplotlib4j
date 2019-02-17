package com.github.sh0nk.matplotlib4j;

import java.util.ArrayList;
import java.util.List;

public enum TypeConversion {
    INSTANCE;

    private final static String PYTHON_NONE = "None";

    public List<Object> typeSafeList(List<? extends Number> orgList) {
        List<Object> outList = new ArrayList();
        for (int i = 0; i < orgList.size(); i++) {
            Number x = orgList.get(i);
            if (x == null || (x instanceof Double && Double.isInfinite((double)x))) {
                outList.add(PYTHON_NONE);
            } else {
                outList.add(x);
            }
        }
        return outList;
//                orgList.stream().map(x -> {
//            if (x == null || Double.isInfinite(x)) {
//                return PYTHON_NONE;
//            } else {
//                return x;
//            }
//        }).collect(Collectors.toList());
    }
}
