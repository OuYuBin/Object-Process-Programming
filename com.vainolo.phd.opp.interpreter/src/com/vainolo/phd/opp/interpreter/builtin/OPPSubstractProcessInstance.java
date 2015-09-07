/*******************************************************************************
 * Copyright (c) 2012 Arieh 'Vainolo' Bibliowicz
 * You can use this code for educational purposes. For any other uses
 * please contact me: vainolo@gmail.com
 *******************************************************************************/
package com.vainolo.phd.opp.interpreter.builtin;

import java.math.BigDecimal;
import java.util.List;

import com.google.common.collect.Lists;
import com.vainolo.phd.opp.interpreter.OPPAbstractProcessInstance;
import com.vainolo.phd.opp.interpreter.OPPObjectInstance;
import com.vainolo.phd.opp.interpreter.OPPParameter;
import com.vainolo.phd.opp.interpreter.OPPProcessInstance;

/**
 * Process that adds two integer values.
 * 
 * @author Arieh 'Vainolo' Bibliowicz
 * @created 9 Jul 2012
 * 
 */
public class OPPSubstractProcessInstance extends OPPAbstractProcessInstance implements OPPProcessInstance {
  @Override
  public void executing() {
    BigDecimal a = getArgument("a").getNumericalValue();
    BigDecimal b = getArgument("b").getNumericalValue();
    BigDecimal c = a.subtract(b);
    OPPObjectInstance instance = OPPObjectInstance.createFromValue(c);
    setArgument("c", instance);
  }

  @Override
  public List<OPPParameter> getIncomingParameters() {
    return Lists.newArrayList(new OPPParameter("a"), new OPPParameter("b"));
  }

  @Override
  public List<OPPParameter> getOutgoingParameters() {
    return Lists.newArrayList(new OPPParameter("c"));
  }
}
