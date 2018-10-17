package org.bme.mit.iir;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerRuntimeException;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class ReasoningExample {

    public static final String PCSHOP_ONTOLOGY_FNAME =
            "c:/temp/pc_shop/pc_shop.owl.xml";
    public static final String PCSHOP_BASE_URI =
            "http://mit.bme.hu/ontologia/iir_labor/pc_shop.owl#";
    public static final IRI ANNOTATION_TYPE_IRI =
            OWLRDFVocabulary.RDFS_LABEL.getIRI();

    OWLOntologyManager manager;
    OWLOntology ontology;
    OWLReasoner reasoner;
    OWLDataFactory factory;

    public ReasoningExample(String ontologyFilename) {
        manager = OWLManager.createOWLOntologyManager();

        ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(
                    new File(ontologyFilename));
        } catch (Exception e) {
            System.err.println("Hiba az ontológia betöltése közben:\n\t");
            e.printStackTrace();
            System.exit(-1);
        }

        OWLReasonerFactory reasonerFactory = new org.semanticweb.HermiT.Reasoner.ReasonerFactory();
        
        reasoner = reasonerFactory.createReasoner(ontology);

        try {
            if (!reasoner.isConsistent()) {
                System.err.println("Az ontológia nem konzisztens!");

                Node<OWLClass> incClss = reasoner.getUnsatisfiableClasses();
                System.err.println("A következő osztályok nem konzisztensek: "
                        + Util.join(incClss.getEntities(), ", ") + ".");
                System.exit(-1);
            }
        } catch (OWLReasonerRuntimeException e) {
            System.err.println("Hiba a következtetében: " + e.getMessage());
            System.exit(-1);
        }
        factory = manager.getOWLDataFactory();
    }

    public Set<OWLClass> getSubClasses(String className, boolean direct) {
        IRI clsIRI = IRI.create(PCSHOP_BASE_URI + className);
        if (!ontology.containsClassInSignature(clsIRI)) {
            System.out.println("Nincs ilyen osztály az ontológiában: \"" +
                    className + "\"");
            return Collections.emptySet();
        }
        OWLClass cls = factory.getOWLClass(clsIRI);
        NodeSet<OWLClass> subClss;
        try {
            subClss = reasoner.getSubClasses(cls, direct);
        } catch (OWLReasonerRuntimeException e) {
            System.err.println("Hiba az alosztályok következtetése közben: "
                    + e.getMessage());
            return Collections.emptySet();
        }
        return subClss.getFlattened();
    }
    
    public Set<OWLClass> getSuperClasses(String className, boolean direct) {
        IRI clsIRI = IRI.create(PCSHOP_BASE_URI + className);
        if (!ontology.containsClassInSignature(clsIRI)) {
            System.out.println("Nincs ilyen osztály az ontológiában: \"" +
                    className + "\"");
            return Collections.emptySet();
        }
        OWLClass cls = factory.getOWLClass(clsIRI);
        NodeSet<OWLClass> superClss;
        try {
            superClss = reasoner.getSuperClasses(cls, direct);
        } catch (OWLReasonerRuntimeException e) {
            System.err.println("Hiba az alosztályok következtetése közben: "
                    + e.getMessage());
            return Collections.emptySet();
        }
        return superClss.getFlattened();
    }

    public Set<String> getClassAnnotations(OWLEntity entity) {
        OWLAnnotationProperty label =
                factory.getOWLAnnotationProperty(ANNOTATION_TYPE_IRI);
        Set<String> result = new HashSet<String>();
        for (OWLAnnotation a : entity.getAnnotations(ontology, label)) {
            if (a.getValue() instanceof OWLLiteral) {
                OWLLiteral value = (OWLLiteral)a.getValue();
                result.add(value.getLiteral());
            }
        }
        return Collections.unmodifiableSet(result);
    }
    public static void main(String[] args) {
        ReasoningExample p = new ReasoningExample(
                PCSHOP_ONTOLOGY_FNAME);

        final String term = "alkatrész";
        Set<OWLClass> descendants = p.getSubClasses(term, false);
        System.out.println("Query expansion a leszármazottak szerint: ");
        for (OWLClass cls : descendants) {
            if (!cls.isBuiltIn()) {
                Set<String> labels = p.getClassAnnotations(cls);
                System.out.println("\t- "
                        + term + " -> " + cls.getIRI().getFragment()
                        + " [" + Util.join(labels, ", ") + "]");
            }
        }
        System.out.println("Query expansion a ősök szerint: ");
        Set<OWLClass> supers = p.getSuperClasses(term, false);
        for (OWLClass cls : supers) {
            if (!cls.isBuiltIn()) {
                Set<String> labels = p.getClassAnnotations(cls);
                System.out.println("\t- "
                        + term + " <- " + cls.getIRI().getFragment()
                        + " [" + Util.join(labels, ", ") + "]");
            }
        }
        
    }
}
