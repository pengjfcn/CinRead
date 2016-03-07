package com.cinread.ebook.pdf;

/**
 * @Project: CinRead
 * @Package: com.cinread.ebook.pdf
 * @Author: Think
 * @Time: 2016/3/7
 * @desc: TODO
 */
public class SelectPDFItem {
    enum Type {
        PARENT, DIR, DOC, IMG
    }

    final public Type type;
    final public String name;

    public SelectPDFItem (Type t, String n) {
        type = t;
        name = n;
    }
}
