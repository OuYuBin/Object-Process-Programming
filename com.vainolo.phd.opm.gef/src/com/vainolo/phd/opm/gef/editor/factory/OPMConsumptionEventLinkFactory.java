/*******************************************************************************
 * Copyright (c) 2012 Arieh 'Vainolo' Bibliowicz
 * You can use this code for educational purposes. For any other uses
 * please contact me: vainolo@gmail.com
 *******************************************************************************/
package com.vainolo.phd.opm.gef.editor.factory;

import org.eclipse.gef.requests.CreationFactory;

import com.vainolo.phd.opm.model.OPMFactory;
import com.vainolo.phd.opm.model.OPMProceduralLink;
import com.vainolo.phd.opm.model.OPMProceduralLinkKind;

/**
 * Factory used by palette tools to create {@link OPMProceduralLink} of {@link OPMProceduralLinkKind#CONSUMPTION_EVENT}
 * kind.
 */
public class OPMConsumptionEventLinkFactory implements CreationFactory {

	@Override
	public Object getNewObject() {
		OPMProceduralLink link = OPMFactory.eINSTANCE.createOPMProceduralLink();
		link.setKind(OPMProceduralLinkKind.CONSUMPTION_EVENT);
		return link;
	}

	@Override
	public Object getObjectType() {
		return OPMProceduralLink.class;
	}

}