package org.accounting.system.repositories.modulators;

import com.mongodb.MongoWriteException;
import org.accounting.system.entities.Entity;
import org.accounting.system.entities.acl.AccessControl;
import org.accounting.system.exceptions.ConflictException;

import java.util.List;

/**
 * This {@link AbstractModulator} determines which {@link AccessModulator} will take over
 * the execution of queries and also delegates their execution to the corresponding {@link AccessModulator}.
 *
 * @param <E> Generic class that represents a mongo collection.
 */
public abstract class AbstractModulator<E extends Entity, I> extends AccessModulator<E, I>{

    @Override
    public  E fetchEntityById(I id){
        return get().fetchEntityById(id);
    }

    @Override
    public  boolean deleteEntityById(I id){
        return get().deleteEntityById(id);
    }

    @Override
    public E updateEntity(E entity, I id){
        return get().updateEntity(entity, id);
    }

    @Override
    public  List<E> getAllEntities(){
        return get().getAllEntities();
    }

    @Override
    public void grantPermission(AccessControl accessControl){
        try{
            get().grantPermission(accessControl);
        } catch (MongoWriteException e){
            throw new ConflictException("There is already an Access Control Entry with this {who, collection, entity} : {" + accessControl.getWho()+", "+accessControl.getCollection()+", "+accessControl.getEntity()+"}");
        }
    }

    @Override
    public void modifyPermission(AccessControl accessControl) {
        get().modifyPermission(accessControl);
    }

    @Override
    public void deletePermission(AccessControl accessControl) {
         get().deletePermission(accessControl);
    }

    @Override
    public AccessControl getPermission(String entity, String who) {
        return get().getPermission(entity, who);
    }

    @Override
    public List<AccessControl> getAllPermissions() {
        return get().getAllPermissions();
    }

    public abstract AccessAlwaysModulator<E, I> always();

    public abstract AccessEntityModulator<E, I> entity();

    public AccessModulator<E, I> get(){

        switch (getRequestInformation().getAccessType()){
            case ALWAYS:
                return always();
            case ENTITY:
                return entity();
            default:
                return always();
        }
    }

}