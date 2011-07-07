package com.vainolo.phd.opm.gef.editor.part;

import com.vainolo.phd.opm.model.OPMObject;
import com.vainolo.phd.opm.model.OPMObjectProcessDiagram;
import com.vainolo.phd.opm.model.OPMProcess;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

public class OPMEditPartFactory implements EditPartFactory {

	@Override public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		
		if(model instanceof OPMObjectProcessDiagram) {
			part = new OPMObjectProcessDiagramEditPart();
		} else if(model instanceof OPMObject) {
			part = new OPMObjectEditPart();
		} else if(model instanceof OPMProcess) {
			part = new OPMProcessEditPart();
		}
		
		if(part != null) {
			part.setModel(model);
		}
		
		return part;
	}
}