package org.imagopole.omero.tools.api.blitz;

import java.util.Collection;
import java.util.List;

import omero.ServerError;
import omero.model.IObject;

public interface OmeroUpdateService {

    Collection<IObject> saveAll(List<IObject> iObjects) throws ServerError;

}
