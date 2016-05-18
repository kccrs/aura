/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.integration.test.root.intf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.auraframework.def.AttributeDef;
import org.auraframework.def.DefDescriptor;
import org.auraframework.def.EventDef;
import org.auraframework.def.InterfaceDef;
import org.auraframework.def.ProviderDef;
import org.auraframework.def.RegisterEventDef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.FakeRegistry;
import org.auraframework.impl.root.AttributeDefImpl;
import org.auraframework.impl.root.event.RegisterEventDefImpl;
import org.auraframework.impl.root.intf.InterfaceDefImpl;
import org.auraframework.impl.system.DefDescriptorImpl;
import org.auraframework.system.Location;
import org.auraframework.test.source.StringSource;
import org.auraframework.throwable.quickfix.DefinitionNotFoundException;
import org.auraframework.throwable.quickfix.InvalidDefinitionException;
import org.junit.Test;

public class InterfaceDefTest extends AuraImplTestCase {

    public InterfaceDefTest(String name) {
        super(name);
    }

    @Test
    public void testHashCode() {
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null,
                vendor.makeLocation("filename1", 5, 5, 0), null, null);
        assertEquals(
                "Hashcode should be the same for both aura:testinterfacechild defs",
                vendor.makeInterfaceDefWithNulls(vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null,
                        null, vendor.makeLocation("filename1", 5, 5, 0), null, null).hashCode(), intDef2.hashCode());
    }

    @Test
    public void testAppendDependencies() throws Exception {
        Set<DefDescriptor<InterfaceDef>> extensions = new HashSet<>();
        extensions.add(vendor.makeInterfaceDefDescriptor("aura:testinterfaceparent"));
        Map<String, RegisterEventDef> eventDefs = new HashMap<>();
        DefDescriptor<EventDef> eventDescriptor = DefDescriptorImpl.getInstance("aura:testevent", EventDef.class);
        RegisterEventDef red = vendor.makeRegisterEventDefWithNulls(eventDescriptor, true, null);
        eventDefs.put("buckfutter", red);
        InterfaceDefImpl def = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, eventDefs, null, extensions,
                "java://org.auraframework.impl.java.provider.TestComponentDescriptorProvider");
        Set<DefDescriptor<?>> expected = new HashSet<>();
        expected.add(DefDescriptorImpl.getInstance(
                "java://org.auraframework.impl.java.provider.TestComponentDescriptorProvider", ProviderDef.class));
        expected.add(vendor.makeInterfaceDefDescriptor("aura:testinterfaceparent"));
        expected.add(eventDescriptor);
        Set<DefDescriptor<?>> dependencies = new HashSet<>();
        def.appendDependencies(dependencies);
        assertEquals("dependencies are incorrect", expected, dependencies);
    }

    @Test
    public void testValidateDefinitionNullDescriptor() throws Exception {
        InterfaceDefImpl def = vendor.makeInterfaceDefWithNulls(null, null, null, null, null, null);
        try {
            def.validateDefinition();
            fail("Should have thrown InvalidDefinitionException for AuraDescriptor<InterfaceDef> being null");
        } catch (InvalidDefinitionException expected) {
        }
    }

    @Test
    public void testValidateReferences() throws Exception {
        FakeRegistry fake = createFakeRegistry();
        InterfaceDef ed = vendor.makeInterfaceDef();
        InterfaceDef extendsID = vendor.makeInterfaceDef(vendor.getParentInterfaceDefDescriptor());
        fake.putDefinition(extendsID);
        fake.putDefinition(vendor.makeEventDef());
        ed.validateReferences();
    }

    @Test
    public void testValidateValidDefinition() throws Exception {
        DefDescriptor<EventDef> eventDescriptor = DefDescriptorImpl.getInstance("aura:testevent", EventDef.class);
        RegisterEventDefImpl red = vendor.makeRegisterEventDefWithNulls(eventDescriptor, true, null);
        Map<String, RegisterEventDef> eventDefs = new HashMap<>();
        eventDefs.put("buckfutter", red);
        AttributeDefImpl testAttributeDef = new AttributeDefImpl(DefDescriptorImpl.getInstance("testattribute",
                AttributeDef.class), null, vendor.getTypeDef().getDescriptor(), null, false,
                AttributeDef.SerializeToType.BOTH, null);
        Map<DefDescriptor<AttributeDef>, AttributeDef> attDefs = new HashMap<>();
        attDefs.put(DefDescriptorImpl.getInstance("nullAttribute", AttributeDef.class), testAttributeDef);
        InterfaceDefImpl def = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), attDefs, eventDefs, null, null, null);
        def.validateDefinition();
    }

    @Test
    public void testGetRegisterEventDefs() throws Exception {
        Set<DefDescriptor<InterfaceDef>> extendsIntf = new HashSet<>();
        extendsIntf.add(vendor.makeInterfaceDefDescriptor("test:testinterfaceparent"));
        InterfaceDefImpl id = vendor.makeInterfaceDef(extendsIntf);
        Map<String, RegisterEventDef> registeredED = id.getRegisterEventDefs();
        assertEquals(2, registeredED.size());
        assertNotNull(registeredED.get("parentEvent"));
    }

    @Test
    public void testGetAttributeDefs() throws Exception {
        Set<DefDescriptor<InterfaceDef>> extendsIntf = new HashSet<>();
        extendsIntf.add(vendor.makeInterfaceDefDescriptor("test:testinterfaceparent"));
        InterfaceDef id = vendor.makeInterfaceDef(extendsIntf);
        Map<DefDescriptor<AttributeDef>, AttributeDef> attributes = id.getAttributeDefs();
        assertEquals(2, attributes.size());
        assertTrue("Attribute from parent should be in the map",
                attributes.containsKey(DefDescriptorImpl.getInstance("mystring", AttributeDef.class)));
        assertTrue("Attribute from child should be in the map",
                attributes.containsKey(DefDescriptorImpl.getInstance(vendor.getAttributeName(), AttributeDef.class)));
    }

    @Test
    public void testGetEventDefsWithoutExtensions() throws Exception {
        DefDescriptor<EventDef> eventTestDescriptor = DefDescriptorImpl.getInstance("aura:testevent", EventDef.class);
        RegisterEventDef regEventDef = vendor.makeRegisterEventDefWithNulls(eventTestDescriptor, true, null);
        Map<String, RegisterEventDef> eventDefs = new HashMap<>();
        eventDefs.put("cans", regEventDef);
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, eventDefs, null, null, null);
        assertEquals(eventDefs, intDef2.getRegisterEventDefs());
    }

    @Test
    public void testGetAttributeDefsWithoutExtensions() throws Exception {
        Map<DefDescriptor<AttributeDef>, AttributeDef> attributes = new HashMap<>();
        AttributeDef attDef = new AttributeDefImpl(DefDescriptorImpl.getInstance("Fake Attribute", AttributeDef.class),
                null, null, null, false, AttributeDef.SerializeToType.BOTH, null);
        attributes.put(attDef.getDescriptor(), attDef);
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), attributes, null, null, null, null);
        Map<DefDescriptor<AttributeDef>, AttributeDef> returnedAttributes = intDef2.getAttributeDefs();
        assertEquals(1, returnedAttributes.size());
        assertEquals(attDef,
                returnedAttributes.get(DefDescriptorImpl.getInstance("Fake Attribute", AttributeDef.class)));
    }

    @Test
    public void testGetExtendsDescriptor() {
        Set<DefDescriptor<InterfaceDef>> extensions = new HashSet<>();
        extensions.add(vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"));
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfaceparent"), null, null, null, extensions, null);
        assertEquals(extensions, intDef2.getExtendsDescriptors());
    }

    @Test
    public void testEqualsObject() {
        Set<DefDescriptor<InterfaceDef>> extensions = new HashSet<>();
        extensions.add(vendor.makeInterfaceDefDescriptor("aura:testinterfaceparent"));
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null,
                vendor.makeLocation("filename1", 5, 5, 0), extensions, null);
        InterfaceDefImpl intDef3 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null,
                vendor.makeLocation("filename1", 5, 5, 0), extensions, null);
        assertTrue("Two interfaceDefs with the same attributes failed equality", intDef2.equals(intDef3));
    }

    @Test
    public void testEqualsWithDifferentTypes() {
        DefDescriptor<EventDef> eventTestDescriptor = DefDescriptorImpl.getInstance("aura:testevent", EventDef.class);
        RegisterEventDefImpl regEventDef = vendor.makeRegisterEventDefWithNulls(eventTestDescriptor, true, null);
        assertFalse(
                "Two different Defs shouldn't have been equal",
                vendor.makeInterfaceDef(vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null,
                        vendor.makeLocation("filename1", 5, 5, 0), null).equals(regEventDef));
    }

    @Test
    public void testEqualsWithDifferentExtensions() {
        Set<DefDescriptor<InterfaceDef>> extensions = new HashSet<>();
        extensions.add(vendor.makeInterfaceDefDescriptor("aura:testinterfaceparent"));
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null,
                vendor.makeLocation("filename1", 5, 5, 0), extensions, null);
        assertFalse(
                "InterfacesDefs with different extensions shouldn't have been equal",
                vendor.makeInterfaceDef(vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null,
                        vendor.makeLocation("filename1", 5, 5, 0), null).equals(intDef2));
    }

    @Test
    public void testEqualsWithDifferentLocations() {
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(vendor
                .makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null, new Location("filename1", 4, 4,
                        1000), null, null);
        assertFalse(
                "InterfacesDefs with different locations shouldn't have been equal",
                vendor.makeInterfaceDef(vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), null, null,
                        vendor.makeLocation("filename1", 5, 5, 0), null).equals(intDef2));
    }

    @Test
    public void testSerialize() throws Exception {
        Set<DefDescriptor<InterfaceDef>> extensions = new HashSet<>();
        extensions.add(vendor.makeInterfaceDefDescriptor("aura:testinterfaceparent"));
        Map<DefDescriptor<AttributeDef>, AttributeDef> attributes = new HashMap<>();
        AttributeDef attDef = new AttributeDefImpl(DefDescriptorImpl.getInstance("Fake Attribute", AttributeDef.class),
                null, null, null, false, AttributeDef.SerializeToType.BOTH, null);
        attributes.put(attDef.getDescriptor(), attDef);
        DefDescriptor<EventDef> eventTestDescriptor = DefDescriptorImpl.getInstance("aura:testevent", EventDef.class);
        RegisterEventDef regEventDef = vendor.makeRegisterEventDefWithNulls(eventTestDescriptor, true, null);
        Map<String, RegisterEventDef> eventDefs = new HashMap<>();
        eventDefs.put("ass", regEventDef);
        InterfaceDefImpl intDef2 = vendor.makeInterfaceDefWithNulls(
                vendor.makeInterfaceDefDescriptor("aura:testinterfacechild"), attributes, eventDefs,
                vendor.makeLocation("filename1", 5, 5, 0), extensions, null);
        serializeAndGoldFile(intDef2);
    }

    @Test
    public void testExtendsItself() throws Exception {
        DefDescriptor<InterfaceDef> extendsSelf = addSourceAutoCleanup(InterfaceDef.class, "");
        StringSource<?> source = (StringSource<?>) getSource(extendsSelf);
        source.addOrUpdate(String.format("<aura:interface extends='%s'> </aura:interface>",
                extendsSelf.getDescriptorName()));
        try {
            InterfaceDef def = extendsSelf.getDef();
            def.validateReferences();
            fail("An interface should not be able to extend itself.");
        } catch (InvalidDefinitionException expected) {
            assertEquals(extendsSelf.getQualifiedName() + " cannot extend itself", expected.getMessage());
        }
    }

    @Test
    public void testExtendsNonExistent() {
        DefDescriptor<InterfaceDef> cmpDesc = addSourceAutoCleanup(InterfaceDef.class,
                "<aura:interface extends='aura:iDontExist'></aura:interface>");
        try {
            // Aura.getInstanceService().getInstance(cmpDesc.getDescriptorName(), ComponentDef.class);
            InterfaceDef def = cmpDesc.getDef();
            def.validateReferences();
            fail("Did not get expected exception: " + DefinitionNotFoundException.class.getName());
        } catch (Exception e) {
            checkExceptionFull(e, DefinitionNotFoundException.class,
                    "No INTERFACE named markup://aura:iDontExist found : [" + cmpDesc.getQualifiedName()+"]",
                    cmpDesc.getQualifiedName());
        }
    }

    @Test
    public void testImplementsAnInterface() throws Exception {
        DefDescriptor<InterfaceDef> d = addSourceAutoCleanup(InterfaceDef.class,
                "<aura:interface implements='test:fakeInterface'> </aura:interface>");
        try {
            d.getDef();
            fail("An interface cannot implement another interface, it can only extend it.");
        } catch (InvalidDefinitionException expected) {
        }
    }
}
