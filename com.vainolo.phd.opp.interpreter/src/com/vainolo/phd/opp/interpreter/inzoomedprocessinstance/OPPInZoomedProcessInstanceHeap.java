package com.vainolo.phd.opp.interpreter.inzoomedprocessinstance;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import static com.google.common.base.Preconditions.*;
import static com.vainolo.phd.opp.utilities.OPPLogger.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.vainolo.phd.opp.interpreter.OPPObjectInstance;
import com.vainolo.phd.opp.interpreter.OPPObjectInstanceValueAnalyzer;
import com.vainolo.phd.opp.interpreter.OPPProcessInstanceHeap;
import com.vainolo.phd.opp.interpreter.OPPRuntimeException;
import com.vainolo.phd.opp.model.OPPObject;
import com.vainolo.phd.opp.model.OPPObjectProcessDiagram;
import com.vainolo.phd.opp.model.OPPProceduralLink;
import com.vainolo.phd.opp.model.OPPProcess;
import com.vainolo.phd.opp.utilities.analysis.OPPOPDAnalyzer;

public class OPPInZoomedProcessInstanceHeap extends OPPProcessInstanceHeap {

  private OPPOPDAnalyzer analyzer;
  private final Map<OPPObject, OPPObjectInstance> variables;
  private OPPObjectInstanceValueAnalyzer valueAnalyzer;
  private Observable observable;

  @Inject
  OPPInZoomedProcessInstanceHeap(OPPObjectInstanceValueAnalyzer valueAnalyzer, OPPOPDAnalyzer analyzer) {
    this.valueAnalyzer = valueAnalyzer;
    this.analyzer = analyzer;
    this.variables = Maps.newHashMap();
    this.observable = new OPMHeapObservable();
  }

  /**
   * <p>
   * Set the value in an {@link OPPObject}.
   * </p>
   * 
   * <p>
   * If the {@link OPPObject} is a part of another {@link Object}, the parent
   * {@link OPPObject} is updated. If the {@link OPPObject} is part of another
   * {@link OPPObject}, but the parent doesn't exist yet, it is created.
   * </p>
   * 
   * <p>
   * If the {@link OPPObject} has outgoing data links to other {@link OPPObject}
   * s, these {@link OPPObject}s are updated (recursively if necessary).
   * 
   * 
   * @param object
   *          where a value can be stored
   * @param value
   *          the value to store
   */
  public void setVariable(OPPObject object, OPPObjectInstance value) {
    logFiner("Setting value of object {0} with value {1}.", object.getName(), value.toString());
    checkArgument(value != null, "Value cannot be null");

    if (analyzer.isObjectPartOfAnotherObject(object)) {
      OPPObject parentObject = analyzer.findParent(object);
      OPPObjectInstance parentValue = getVariable(parentObject);
      if (parentValue == null) {
        parentValue = OPPObjectInstance.createCompositeInstance();
      }
      setVariable(parentObject, parentValue);
      parentValue = getVariable(parentObject);
      parentValue.setPart(object.getName(), OPPObjectInstance.createFromExistingInstance(value));
      observable.notifyObservers(new OPMHeapChange(parentObject, parentValue, object, getVariable(object)));
    } else {
      OPPObjectInstance objectValue = OPPObjectInstance.createFromExistingInstance(value);
      variables.put(object, objectValue);
      observable.notifyObservers(new OPMHeapChange(object, objectValue));
    }
    transferDataFromObject(object);
  }

  /**
   * Return the value stored in the {@link OPPObject}. If the {@link OPPObject}
   * is part of another {@link OPPObject}, the value if fetched from the parent
   * {@link OPPObject}.
   * 
   * @param object
   *          where a value can be stored
   * @return the value of the {@link OPPObject}, or <code>null</code> if no
   *         value has been assigned.
   */
  public OPPObjectInstance getVariable(OPPObject object) {
    if (analyzer.isObjectPartOfAnotherObject(object)) {
      OPPObjectInstance parent = getVariable(analyzer.findParent(object));
      if (parent == null) {
        logSevere("Tried to get the value of {0} which is part of another object, but parent object doesn't exist.", object.getName());
        throw new IllegalStateException("Getting value of an object which is part of another object, but parent doesn't exist.");
      } else {
        logFinest("Getting value of {0} which is {1}.", object.getName(), parent.getPart(object.getName()));
        return parent.getPart(object.getName());
      }
    } else {
      logFinest("Getting value of {0} which is {1}.", object.getName(), variables.get(object));
      return variables.get(object);
    }
  }

  /**
   * Transfer the data in an {@link OPPObject} through outgoing data links to
   * other {@link OPPObject}'s, also doing this recursively if needed.
   * 
   * @param source
   */
  private void transferDataFromObject(OPPObject source) {
    Collection<OPPProceduralLink> dataTransferLinks = analyzer.findOutgoingDataLinks(source);
    for (OPPProceduralLink link : dataTransferLinks) {
      if (analyzer.isTargetProcess(link))
        continue;

      OPPObject target = analyzer.getTargetObject(link);

      if ((link.getCenterDecoration() == null) || (link.getCenterDecoration().equals(""))) {
        setVariable(target, getVariable(source));
      } else if (!link.getCenterDecoration().contains(",")) {
        transferDataWithOneReference(source, link, target);
      } else {
        transferDataWithTwoReferences(source, link, target);
      }
    }
  }

  private void transferDataWithOneReference(OPPObject source, OPPProceduralLink link, OPPObject target) {
    throw new OPPRuntimeException("Data transfer modifiers are not supported.");
  }

  private void transferDataWithTwoReferences(OPPObject source, OPPProceduralLink link, OPPObject target) {
    throw new OPPRuntimeException("Double data link modifiers are not supported");
  }

  /**
   * Calculate the value of an {@link OPPObject} literal, and set the value of
   * the {@link OPPObject} variable with the literal value.
   * 
   * @param object
   *          that is being analyzed.
   */
  public void calculateOPMObjectValueAndSetVariableIfValueIfExists(OPPObject object) {
    OPPObjectInstance objectValue = valueAnalyzer.calculateOPMObjectValue(object, analyzer);
    if (objectValue != null)
      setVariable(object, objectValue);
  }

  /**
   * Initialize local variables from literals
   */
  public void initializeVariablesWithLiterals(OPPProcess mainProcess) {
    Collection<OPPObject> objectVariables = analyzer.findObjects(mainProcess);
    for (OPPObject object : objectVariables) {
      calculateOPMObjectValueAndSetVariableIfValueIfExists(object);
      if (getVariable(object) != null)
        transferDataFromObject(object);
    }
  }

  /**
   * Create a variable for all of the arguments that were passed to the process,
   * or for arguments that contain literal values.
   */
  public void initializeVariablesWithArgumentValues(OPPObjectProcessDiagram opd) {
    Collection<OPPObject> objectArguments = analyzer.findParameters(opd);
    for (OPPObject object : objectArguments) {
      if (getArgument(object.getName()) != null) {
        setVariable(object, getArgument(object.getName()));
      } else {
        calculateOPMObjectValueAndSetVariableIfValueIfExists(object);
      }
      transferDataFromObject(object);
    }
  }

  /**
   * Initialize variables of the process instance. Variables are initialized
   * from two sources: arguments and literals.
   * 
   * @param opd
   *          that is being executed.
   */
  public void initializeVariables(OPPObjectProcessDiagram opd) {
    initializeVariablesWithArgumentValues(opd);
    initializeVariablesWithLiterals(analyzer.getInZoomedProcess(opd));
  }

  /**
   * Copy the value stored in variables matching process arguments to the
   * external arguments.
   * 
   * @param opd
   *          The Object Process Diagram that contains the variables and the
   *          arguments.
   */
  public void exportVariableValuesToArguments(OPPObjectProcessDiagram opd) {
    Collection<OPPObject> objectArguments = analyzer.findParameters(opd);
    for (OPPObject object : objectArguments) {
      if (getVariable(object) != null) {
        addArgument(object.getName(), getVariable(object));
      }
    }
  }

  /**
   * Add an observer to changes in the {@link OPPInZoomedProcessInstanceHeap}
   * 
   * @param observer
   *          a new observer for this {@link OPPInZoomedProcessInstanceHeap}
   */
  public void addObserver(Observer observer) {
    observable.addObserver(observer);
  }

  class OPMHeapObservable extends Observable {
    @Override
    public void notifyObservers(Object arg) {
      setChanged();
      super.notifyObservers(arg);
    }
  }

  static class OPMHeapObserver implements Observer {
    private List<OPMHeapChange> changes = Lists.newArrayList();

    @Override
    public void update(Observable o, Object arg) {
      changes.add(0, OPMHeapChange.class.cast(arg));
    }

    public void clear() {
      changes.clear();
    }

    public List<OPMHeapChange> getChanges() {
      return Collections.unmodifiableList(changes);
    }

    public Set<OPPObject> getObjectsWithNewValue() {
      Set<OPPObject> objects = Sets.newHashSet();
      for (OPMHeapChange change : changes) {
        if (change.changeType.equals(OPMHeapChangeType.VARIABLE_SET)) {
          objects.add(change.object);
        }
      }
      return objects;
    }
  }

  enum OPMHeapChangeType {
    VARIABLE_SET, PART_ADDED
  }

  class OPMHeapChange {
    public OPMHeapChangeType changeType;
    public OPPObject object;
    public OPPObjectInstance objectInstance;
    public OPPObject child;
    public OPPObjectInstance childInstance;

    public OPMHeapChange(OPPObject object, OPPObjectInstance instance) {
      this.changeType = OPMHeapChangeType.VARIABLE_SET;
      this.object = object;
      this.objectInstance = instance;
    }

    public OPMHeapChange(OPPObject parent, OPPObjectInstance parentInstance, OPPObject child, OPPObjectInstance childInstance) {
      this.changeType = OPMHeapChangeType.PART_ADDED;
      this.object = parent;
      this.objectInstance = parentInstance;
      this.child = child;
      this.childInstance = childInstance;
    }
  }
}
