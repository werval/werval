package org.qiweb.http.controllers;

import java.util.List;
import org.codeartisans.java.toolbox.Couple;

public interface ImprovedResult
{

    int status();

    Iterable<Couple<String, List<String>>> headers();
}