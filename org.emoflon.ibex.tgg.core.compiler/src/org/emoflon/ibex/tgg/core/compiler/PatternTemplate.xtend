package org.emoflon.ibex.tgg.core.compiler

import java.util.Map
import org.emoflon.ibex.tgg.core.compiler.pattern.protocol.ConsistencyPattern
import org.emoflon.ibex.tgg.core.compiler.pattern.rulepart.RulePartPattern
import org.emoflon.ibex.tgg.core.compiler.pattern.protocol.nacs.PatternWithProtocolNACs

class PatternTemplate {
		
	def generateHeaderAndImports(Map<String, String> imports, String ruleName){
		return '''
		package org.emoflon.ibex.tgg.«ruleName.toLowerCase»
		
		«FOR p : imports.keySet»
			import "«imports.get(p)»" as «p»
		«ENDFOR»
		
		'''
	}
		
	def generateOperationalPattern(RulePartPattern pattern) {
		return '''
		pattern «pattern.getName»(«FOR e : pattern.getSignatureElements SEPARATOR ", "»«e.name»:«pattern.typeOf(e).name»«ENDFOR»){
			«FOR edge : pattern.getBodyEdges»
			Edge.src(«edge.name»,«edge.srcNode.name»);
			Edge.trg(«edge.name»,«edge.trgNode.name»);
			Edge.name(«edge.name»,"«edge.type.name»");
			«ENDFOR»			
			«FOR node : pattern.bodySrcTrgNodes»
			«node.type.name»(«node.name»);
			«ENDFOR»			
			«FOR corr : pattern.bodyCorrNodes»
			«corr.type.name».source(«corr.name»,«corr.source.name»);
			«corr.type.name».target(«corr.name»,«corr.target.name»);
			«ENDFOR»
			«FOR pi : pattern.positiveInvocations»
			find «pi.getName»(«FOR e : pi.signatureElements SEPARATOR ", "»«e.name»«ENDFOR»);
			«ENDFOR»
			«FOR ni : pattern.negativeInvocations»
			neg find «ni.getName»(«FOR e : ni.signatureElements SEPARATOR ", "»«e.name»«ENDFOR»);
			«ENDFOR»
		    check(true);
		}
		
		'''
	}
	
	def generateProtocolNACsPattern(PatternWithProtocolNACs pattern) {
		return '''
		pattern «pattern.getName»(«FOR e : pattern.getSignatureElements SEPARATOR ", "»«e.name»:«pattern.typeOf(e).name»«ENDFOR»){
			«FOR pi : pattern.positiveInvocations»
			find «pi.getName»(«FOR e : pi.signatureElements SEPARATOR ", "»«e.name»«ENDFOR»);
			«ENDFOR»
			«FOR e : pattern.NACrelevantElements»
			neg find marked(«e.name»);
			«ENDFOR»
		}
		'''
	}
	
	def generateConsistencyPattern(ConsistencyPattern pattern) {
		return '''
		pattern «pattern.getName»(«FOR e : pattern.getSignatureElements SEPARATOR ", "»«e.name»:«pattern.typeOf(e).name»«ENDFOR»){
			«FOR e : pattern.contextSrc»
			TGGRuleApplication.contextSrc(«pattern.protocolNodeName», «e.name»);
			«ENDFOR»
			«FOR e : pattern.createdSrc»
			TGGRuleApplication.createdSrc(«pattern.protocolNodeName», «e.name»);
			«ENDFOR»
			«FOR e : pattern.contextTrg»
			TGGRuleApplication.contextTrg(«pattern.protocolNodeName», «e.name»);
			«ENDFOR»
			«FOR e : pattern.createdTrg»
			TGGRuleApplication.createdTrg(«pattern.protocolNodeName», «e.name»);
			«ENDFOR»
			«FOR e : pattern.contextCorr»
			TGGRuleApplication.contextCorr(«pattern.protocolNodeName», «e.name»);
		    «ENDFOR»
		    «FOR e : pattern.createdCorr»
		    TGGRuleApplication.createdCorr(«pattern.protocolNodeName», «e.name»);
		    «ENDFOR»
		}
		'''
	}
}