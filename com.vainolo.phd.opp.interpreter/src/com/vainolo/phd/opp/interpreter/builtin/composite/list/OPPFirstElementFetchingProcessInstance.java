package com.vainolo.phd.opp.interpreter.builtin.composite.list;

import java.util.List;

import com.google.common.collect.Lists;
import com.vainolo.phd.opp.interpreter.OPPAbstractProcessInstance;
import com.vainolo.phd.opp.interpreter.OPPObjectInstance;
import com.vainolo.phd.opp.interpreter.OPPParameter;

public class OPPFirstElementFetchingProcessInstance extends OPPAbstractProcessInstance {

  @Override
  protected void executing() throws Exception {
    OPPObjectInstance list = getArgument("list");
    OPPObjectInstance newList = OPPObjectInstance.createFromExistingInstance(list);
    OPPObjectInstance element = newList.getFirstPart();
    if (element != null) {
      setArgument("element", element);
      setArgument("fetched?", OPPObjectInstance.createFromValue("yes"));
    } else {
      setArgument("fetched?", OPPObjectInstance.createFromValue("no"));
    }
  }

  @Override
  public String getName() {
    return "First Element Fetching";
  }

  @Override
  public List<OPPParameter> getIncomingParameters() {
    return Lists.newArrayList(new OPPParameter("list"));
  }

  @Override
  public List<OPPParameter> getOutgoingParameters() {
    return Lists.newArrayList(new OPPParameter("element"), new OPPParameter("fetched?"));
  }
}
