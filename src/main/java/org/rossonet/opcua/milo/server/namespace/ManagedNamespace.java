/*
 * Copyright (c) 2021 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.rossonet.opcua.milo.server.namespace;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ulong;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.ValueRank;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.Lifecycle;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.dtd.DataTypeDictionaryManager;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.BaseEventTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.variables.AnalogItemTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaDataTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectTypeNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.nodes.factories.NodeFactory;
import org.eclipse.milo.opcua.sdk.server.nodes.filters.AttributeFilters;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.BuiltinDataType;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.XmlElement;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.StructureType;
import org.eclipse.milo.opcua.stack.core.types.structured.EnumDefinition;
import org.eclipse.milo.opcua.stack.core.types.structured.EnumDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.EnumField;
import org.eclipse.milo.opcua.stack.core.types.structured.Range;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDefinition;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.StructureField;
import org.rossonet.opcua.milo.server.Ar4kOpcUaServer;
import org.rossonet.opcua.milo.server.namespace.method.GenerateEventMethod;
import org.rossonet.opcua.milo.server.namespace.method.ShutdownMethod;
import org.rossonet.opcua.milo.server.namespace.method.SqrtMethod;
import org.rossonet.opcua.milo.server.namespace.type.CustomEnumType;
import org.rossonet.opcua.milo.server.namespace.type.CustomStructType;
import org.rossonet.opcua.milo.server.namespace.type.CustomUnionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedNamespace extends ManagedNamespaceWithLifecycle {

	public static final String NAMESPACE_URI = "urn:eclipse:milo:hello-world";

	private static final Logger logger = LoggerFactory.getLogger(ManagedNamespace.class);

	private static final Object[][] STATIC_ARRAY_NODES = new Object[][] {
			{ "BooleanArray", Identifiers.Boolean, false }, { "ByteArray", Identifiers.Byte, ubyte(0) },
			{ "SByteArray", Identifiers.SByte, (byte) 0x00 }, { "Int16Array", Identifiers.Int16, (short) 16 },
			{ "Int32Array", Identifiers.Int32, 32 }, { "Int64Array", Identifiers.Int64, 64L },
			{ "UInt16Array", Identifiers.UInt16, ushort(16) }, { "UInt32Array", Identifiers.UInt32, uint(32) },
			{ "UInt64Array", Identifiers.UInt64, ulong(64L) }, { "FloatArray", Identifiers.Float, 3.14f },
			{ "DoubleArray", Identifiers.Double, 3.14d }, { "StringArray", Identifiers.String, "string value" },
			{ "DateTimeArray", Identifiers.DateTime, DateTime.now() },
			{ "GuidArray", Identifiers.Guid, UUID.randomUUID() },
			{ "ByteStringArray", Identifiers.ByteString, new ByteString(new byte[] { 0x01, 0x02, 0x03, 0x04 }) },
			{ "XmlElementArray", Identifiers.XmlElement, new XmlElement("<a>hello</a>") },
			{ "LocalizedTextArray", Identifiers.LocalizedText, LocalizedText.english("localized text") },
			{ "QualifiedNameArray", Identifiers.QualifiedName, new QualifiedName(1234, "defg") },
			{ "NodeIdArray", Identifiers.NodeId, new NodeId(1234, "abcd") } };

	private static final Object[][] STATIC_SCALAR_NODES = new Object[][] {
			{ "Boolean", Identifiers.Boolean, new Variant(false) },
			{ "Byte", Identifiers.Byte, new Variant(ubyte(0x00)) },
			{ "SByte", Identifiers.SByte, new Variant((byte) 0x00) },
			{ "Integer", Identifiers.Integer, new Variant(32) },
			{ "Int16", Identifiers.Int16, new Variant((short) 16) }, { "Int32", Identifiers.Int32, new Variant(32) },
			{ "Int64", Identifiers.Int64, new Variant(64L) },
			{ "UInteger", Identifiers.UInteger, new Variant(uint(32)) },
			{ "UInt16", Identifiers.UInt16, new Variant(ushort(16)) },
			{ "UInt32", Identifiers.UInt32, new Variant(uint(32)) },
			{ "UInt64", Identifiers.UInt64, new Variant(ulong(64L)) },
			{ "Float", Identifiers.Float, new Variant(3.14f) }, { "Double", Identifiers.Double, new Variant(3.14d) },
			{ "String", Identifiers.String, new Variant("string value") },
			{ "DateTime", Identifiers.DateTime, new Variant(DateTime.now()) },
			{ "Guid", Identifiers.Guid, new Variant(UUID.randomUUID()) },
			{ "ByteString", Identifiers.ByteString,
					new Variant(new ByteString(new byte[] { 0x01, 0x02, 0x03, 0x04 })) },
			{ "XmlElement", Identifiers.XmlElement, new Variant(new XmlElement("<a>hello</a>")) },
			{ "LocalizedText", Identifiers.LocalizedText, new Variant(LocalizedText.english("localized text")) },
			{ "QualifiedName", Identifiers.QualifiedName, new Variant(new QualifiedName(1234, "defg")) },
			{ "NodeId", Identifiers.NodeId, new Variant(new NodeId(1234, "abcd")) },
			{ "Variant", Identifiers.BaseDataType, new Variant(32) },
			{ "Duration", Identifiers.Duration, new Variant(1.0) },
			{ "UtcTime", Identifiers.UtcTime, new Variant(DateTime.now()) }, };

	private final DataTypeDictionaryManager dictionaryManager;
	private volatile Thread eventThread;

	private volatile boolean keepPostingEvents = true;

	private final Ar4kOpcUaServer opcUaServer;

	private final Random random = new Random();

	private final SubscriptionModel subscriptionModel;

	public ManagedNamespace(final Ar4kOpcUaServer opcUaServer) {
		super(opcUaServer.getServer(), NAMESPACE_URI);
		this.opcUaServer = opcUaServer;
		subscriptionModel = new SubscriptionModel(opcUaServer.getServer(), this);
		dictionaryManager = new DataTypeDictionaryManager(getNodeContext(), NAMESPACE_URI);
		getLifecycleManager().addLifecycle(dictionaryManager);
		getLifecycleManager().addLifecycle(subscriptionModel);
		getLifecycleManager().addStartupTask(this::createAndAddNodes);
		getLifecycleManager().addLifecycle(new Lifecycle() {
			@Override
			public void shutdown() {
				try {
					keepPostingEvents = false;
					eventThread.interrupt();
					eventThread.join();
				} catch (final InterruptedException ignored) {
					// ignored
				}
			}

			@Override
			public void startup() {
				startBogusEventNotifier();
			}
		});
	}

	@Override
	public void onDataItemsCreated(final List<DataItem> dataItems) {
		subscriptionModel.onDataItemsCreated(dataItems);
	}

	@Override
	public void onDataItemsDeleted(final List<DataItem> dataItems) {
		subscriptionModel.onDataItemsDeleted(dataItems);
	}

	@Override
	public void onDataItemsModified(final List<DataItem> dataItems) {
		subscriptionModel.onDataItemsModified(dataItems);
	}

	@Override
	public void onMonitoringModeChanged(final List<MonitoredItem> monitoredItems) {
		subscriptionModel.onMonitoringModeChanged(monitoredItems);
	}

	private void addAdminReadableNodes(final UaFolderNode rootNode) {
		final UaFolderNode adminFolder = new UaFolderNode(getNodeContext(), newNodeId("HelloWorld/OnlyAdminCanRead"),
				newQualifiedName("OnlyAdminCanRead"), LocalizedText.english("OnlyAdminCanRead"));
		getNodeManager().addNode(adminFolder);
		rootNode.addOrganizes(adminFolder);
		final String name = "String";
		final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/OnlyAdminCanRead/" + name)).setAccessLevel(AccessLevel.READ_WRITE)
				.setBrowseName(newQualifiedName(name)).setDisplayName(LocalizedText.english(name))
				.setDataType(Identifiers.String).setTypeDefinition(Identifiers.BaseDataVariableType).build();
		node.setValue(new DataValue(new Variant("shh... don't tell the lusers")));
		node.getFilterChain().addLast(new RestrictedAccessFilter(identity -> {
			if ("admin".equals(identity)) {
				return AccessLevel.READ_WRITE;
			} else {
				return AccessLevel.NONE;
			}
		}));
		getNodeManager().addNode(node);
		adminFolder.addOrganizes(node);
	}

	private void addAdminWritableNodes(final UaFolderNode rootNode) {
		final UaFolderNode adminFolder = new UaFolderNode(getNodeContext(), newNodeId("HelloWorld/OnlyAdminCanWrite"),
				newQualifiedName("OnlyAdminCanWrite"), LocalizedText.english("OnlyAdminCanWrite"));
		getNodeManager().addNode(adminFolder);
		rootNode.addOrganizes(adminFolder);
		final String name = "String";
		final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/OnlyAdminCanWrite/" + name)).setAccessLevel(AccessLevel.READ_WRITE)
				.setBrowseName(newQualifiedName(name)).setDisplayName(LocalizedText.english(name))
				.setDataType(Identifiers.String).setTypeDefinition(Identifiers.BaseDataVariableType).build();
		node.setValue(new DataValue(new Variant("admin was here")));
		node.getFilterChain().addLast(new RestrictedAccessFilter(identity -> {
			if ("admin".equals(identity)) {
				return AccessLevel.READ_WRITE;
			} else {
				return AccessLevel.READ_ONLY;
			}
		}));

		getNodeManager().addNode(node);
		adminFolder.addOrganizes(node);
	}

	private void addArrayNodes(final UaFolderNode rootNode) {
		final UaFolderNode arrayTypesFolder = new UaFolderNode(getNodeContext(), newNodeId("HelloWorld/ArrayTypes"),
				newQualifiedName("ArrayTypes"), LocalizedText.english("ArrayTypes"));
		getNodeManager().addNode(arrayTypesFolder);
		rootNode.addOrganizes(arrayTypesFolder);
		for (final Object[] os : STATIC_ARRAY_NODES) {
			final String name = (String) os[0];
			final NodeId typeId = (NodeId) os[1];
			final Object value = os[2];
			final Object array = Array.newInstance(value.getClass(), 5);
			for (int i = 0; i < 5; i++) {
				Array.set(array, i, value);
			}
			final Variant variant = new Variant(array);
			UaVariableNode.build(getNodeContext(), builder -> {
				builder.setNodeId(newNodeId("HelloWorld/ArrayTypes/" + name));
				builder.setAccessLevel(AccessLevel.READ_WRITE);
				builder.setUserAccessLevel(AccessLevel.READ_WRITE);
				builder.setBrowseName(newQualifiedName(name));
				builder.setDisplayName(LocalizedText.english(name));
				builder.setDataType(typeId);
				builder.setTypeDefinition(Identifiers.BaseDataVariableType);
				builder.setValueRank(ValueRank.OneDimension.getValue());
				builder.setArrayDimensions(new UInteger[] { uint(0) });
				builder.setValue(new DataValue(variant));
				builder.addAttributeFilter(new AttributeLoggingFilter(AttributeId.Value::equals));
				builder.addReference(new Reference(builder.getNodeId(), Identifiers.Organizes,
						arrayTypesFolder.getNodeId().expanded(), Reference.Direction.INVERSE));
				return builder.buildAndAdd();
			});
		}
	}

	private void addCustomEnumTypeVariable(final UaFolderNode rootFolder) throws Exception {
		final NodeId dataTypeId = CustomEnumType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());
		final UaVariableNode customEnumTypeVariable = UaVariableNode.builder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/CustomEnumTypeVariable")).setAccessLevel(AccessLevel.READ_WRITE)
				.setUserAccessLevel(AccessLevel.READ_WRITE).setBrowseName(newQualifiedName("CustomEnumTypeVariable"))
				.setDisplayName(LocalizedText.english("CustomEnumTypeVariable")).setDataType(dataTypeId)
				.setTypeDefinition(Identifiers.BaseDataVariableType).build();
		customEnumTypeVariable.setValue(new DataValue(new Variant(CustomEnumType.Field1)));
		getNodeManager().addNode(customEnumTypeVariable);
		customEnumTypeVariable.addReference(new Reference(customEnumTypeVariable.getNodeId(), Identifiers.Organizes,
				rootFolder.getNodeId().expanded(), false));
	}

	private void addCustomObjectTypeAndInstance(final UaFolderNode rootFolder) {
		// Define a new ObjectType called "MyObjectType".
		final UaObjectTypeNode objectTypeNode = UaObjectTypeNode.builder(getNodeContext())
				.setNodeId(newNodeId("ObjectTypes/MyObjectType")).setBrowseName(newQualifiedName("MyObjectType"))
				.setDisplayName(LocalizedText.english("MyObjectType")).setIsAbstract(false).build();
		// "Foo" and "Bar" are members. These nodes are what are called "instance
		// declarations" by the spec.
		final UaVariableNode foo = UaVariableNode.builder(getNodeContext())
				.setNodeId(newNodeId("ObjectTypes/MyObjectType.Foo")).setAccessLevel(AccessLevel.READ_WRITE)
				.setBrowseName(newQualifiedName("Foo")).setDisplayName(LocalizedText.english("Foo"))
				.setDataType(Identifiers.Int16).setTypeDefinition(Identifiers.BaseDataVariableType).build();
		foo.addReference(new Reference(foo.getNodeId(), Identifiers.HasModellingRule,
				Identifiers.ModellingRule_Mandatory.expanded(), true));
		foo.setValue(new DataValue(new Variant(0)));
		objectTypeNode.addComponent(foo);
		final UaVariableNode bar = UaVariableNode.builder(getNodeContext())
				.setNodeId(newNodeId("ObjectTypes/MyObjectType.Bar")).setAccessLevel(AccessLevel.READ_WRITE)
				.setBrowseName(newQualifiedName("Bar")).setDisplayName(LocalizedText.english("Bar"))
				.setDataType(Identifiers.String).setTypeDefinition(Identifiers.BaseDataVariableType).build();
		bar.addReference(new Reference(bar.getNodeId(), Identifiers.HasModellingRule,
				Identifiers.ModellingRule_Mandatory.expanded(), true));
		bar.setValue(new DataValue(new Variant("bar")));
		objectTypeNode.addComponent(bar);
		// Tell the ObjectTypeManager about our new type.
		// This let's us use NodeFactory to instantiate instances of the type.
		getServer().getObjectTypeManager().registerObjectType(objectTypeNode.getNodeId(), UaObjectNode.class,
				UaObjectNode::new);
		// Add the inverse SubtypeOf relationship.
		objectTypeNode.addReference(new Reference(objectTypeNode.getNodeId(), Identifiers.HasSubtype,
				Identifiers.BaseObjectType.expanded(), false));
		// Add type definition and declarations to address space.
		getNodeManager().addNode(objectTypeNode);
		getNodeManager().addNode(foo);
		getNodeManager().addNode(bar);
		// Use NodeFactory to create instance of MyObjectType called "MyObject".
		// NodeFactory takes care of recursively instantiating MyObject member nodes
		// as well as adding all nodes to the address space.
		try {
			final UaObjectNode myObject = (UaObjectNode) getNodeFactory().createNode(newNodeId("HelloWorld/MyObject"),
					objectTypeNode.getNodeId());
			myObject.setBrowseName(newQualifiedName("MyObject"));
			myObject.setDisplayName(LocalizedText.english("MyObject"));
			// Add forward and inverse references from the root folder.
			rootFolder.addOrganizes(myObject);
			myObject.addReference(new Reference(myObject.getNodeId(), Identifiers.Organizes,
					rootFolder.getNodeId().expanded(), false));
		} catch (final UaException e) {
			logger.error("Error creating MyObjectType instance: {}", e.getMessage(), e);
		}
	}

	private void addCustomStructTypeVariable(final UaFolderNode rootFolder) throws Exception {
		final NodeId dataTypeId = CustomStructType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

		final NodeId binaryEncodingId = CustomStructType.BINARY_ENCODING_ID
				.toNodeIdOrThrow(getServer().getNamespaceTable());

		final UaVariableNode customStructTypeVariable = UaVariableNode.builder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/CustomStructTypeVariable")).setAccessLevel(AccessLevel.READ_WRITE)
				.setUserAccessLevel(AccessLevel.READ_WRITE).setBrowseName(newQualifiedName("CustomStructTypeVariable"))
				.setDisplayName(LocalizedText.english("CustomStructTypeVariable")).setDataType(dataTypeId)
				.setTypeDefinition(Identifiers.BaseDataVariableType).build();

		final CustomStructType value = new CustomStructType("foo", uint(42), true, CustomEnumType.Field0);

		final ExtensionObject xo = ExtensionObject.encodeDefaultBinary(getServer().getSerializationContext(), value,
				binaryEncodingId);

		customStructTypeVariable.setValue(new DataValue(new Variant(xo)));

		getNodeManager().addNode(customStructTypeVariable);

		customStructTypeVariable.addReference(new Reference(customStructTypeVariable.getNodeId(), Identifiers.Organizes,
				rootFolder.getNodeId().expanded(), false));
	}

	private void addCustomUnionTypeVariable(final UaFolderNode rootFolder) throws Exception {
		final NodeId dataTypeId = CustomUnionType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

		final NodeId binaryEncodingId = CustomUnionType.BINARY_ENCODING_ID
				.toNodeIdOrThrow(getServer().getNamespaceTable());

		final UaVariableNode customUnionTypeVariable = UaVariableNode.builder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/CustomUnionTypeVariable")).setAccessLevel(AccessLevel.READ_WRITE)
				.setUserAccessLevel(AccessLevel.READ_WRITE).setBrowseName(newQualifiedName("CustomUnionTypeVariable"))
				.setDisplayName(LocalizedText.english("CustomUnionTypeVariable")).setDataType(dataTypeId)
				.setTypeDefinition(Identifiers.BaseDataVariableType).build();

		final CustomUnionType value = CustomUnionType.ofBar("hello");

		final ExtensionObject xo = ExtensionObject.encodeDefaultBinary(getServer().getSerializationContext(), value,
				binaryEncodingId);

		customUnionTypeVariable.setValue(new DataValue(new Variant(xo)));

		getNodeManager().addNode(customUnionTypeVariable);

		customUnionTypeVariable.addReference(new Reference(customUnionTypeVariable.getNodeId(), Identifiers.Organizes,
				rootFolder.getNodeId().expanded(), false));
	}

	private void addDataAccessNodes(final UaFolderNode rootNode) {
		// DataAccess folder
		final UaFolderNode dataAccessFolder = new UaFolderNode(getNodeContext(), newNodeId("HelloWorld/DataAccess"),
				newQualifiedName("DataAccess"), LocalizedText.english("DataAccess"));

		getNodeManager().addNode(dataAccessFolder);
		rootNode.addOrganizes(dataAccessFolder);

		try {
			final AnalogItemTypeNode node = (AnalogItemTypeNode) getNodeFactory().createNode(
					newNodeId("HelloWorld/DataAccess/AnalogValue"), Identifiers.AnalogItemType,
					new NodeFactory.InstantiationCallback() {
						@Override
						public boolean includeOptionalNode(final NodeId typeDefinitionId,
								final QualifiedName browseName) {
							return true;
						}
					});

			node.setBrowseName(newQualifiedName("AnalogValue"));
			node.setDisplayName(LocalizedText.english("AnalogValue"));
			node.setDataType(Identifiers.Double);
			node.setValue(new DataValue(new Variant(3.14d)));

			node.setEURange(new Range(0.0, 100.0));

			getNodeManager().addNode(node);
			dataAccessFolder.addOrganizes(node);
		} catch (final UaException e) {
			logger.error("Error creating AnalogItemType instance: {}", e.getMessage(), e);
		}
	}

	private void addDynamicNodes(final UaFolderNode rootNode) {
		final UaFolderNode dynamicFolder = new UaFolderNode(getNodeContext(), newNodeId("HelloWorld/Dynamic"),
				newQualifiedName("Dynamic"), LocalizedText.english("Dynamic"));

		getNodeManager().addNode(dynamicFolder);
		rootNode.addOrganizes(dynamicFolder);

		// Dynamic Boolean
		{
			final String name = "Boolean";
			final NodeId typeId = Identifiers.Boolean;
			final Variant variant = new Variant(false);

			final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
					.setNodeId(newNodeId("HelloWorld/Dynamic/" + name)).setAccessLevel(AccessLevel.READ_WRITE)
					.setBrowseName(newQualifiedName(name)).setDisplayName(LocalizedText.english(name))
					.setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();

			node.setValue(new DataValue(variant));

			node.getFilterChain().addLast(new AttributeLoggingFilter(),
					AttributeFilters.getValue(ctx -> new DataValue(new Variant(random.nextBoolean()))));

			getNodeManager().addNode(node);
			dynamicFolder.addOrganizes(node);
		}

		// Dynamic Int32
		{
			final String name = "Int32";
			final NodeId typeId = Identifiers.Int32;
			final Variant variant = new Variant(0);

			final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
					.setNodeId(newNodeId("HelloWorld/Dynamic/" + name)).setAccessLevel(AccessLevel.READ_WRITE)
					.setBrowseName(newQualifiedName(name)).setDisplayName(LocalizedText.english(name))
					.setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();

			node.setValue(new DataValue(variant));

			node.getFilterChain().addLast(new AttributeLoggingFilter(),
					AttributeFilters.getValue(ctx -> new DataValue(new Variant(random.nextInt()))));

			getNodeManager().addNode(node);
			dynamicFolder.addOrganizes(node);
		}

		// Dynamic Double
		{
			final String name = "Double";
			final NodeId typeId = Identifiers.Double;
			final Variant variant = new Variant(0.0);

			final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
					.setNodeId(newNodeId("HelloWorld/Dynamic/" + name)).setAccessLevel(AccessLevel.READ_WRITE)
					.setBrowseName(newQualifiedName(name)).setDisplayName(LocalizedText.english(name))
					.setDataType(typeId).setTypeDefinition(Identifiers.BaseDataVariableType).build();

			node.setValue(new DataValue(variant));

			node.getFilterChain().addLast(new AttributeLoggingFilter(),
					AttributeFilters.getValue(ctx -> new DataValue(new Variant(random.nextDouble()))));

			getNodeManager().addNode(node);
			dynamicFolder.addOrganizes(node);
		}
	}

	private void addGenerateEventMethod(final UaFolderNode folderNode) {
		final UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/generateEvent(eventTypeId)"))
				.setBrowseName(newQualifiedName("generateEvent(eventTypeId)"))
				.setDisplayName(new LocalizedText(null, "generateEvent(eventTypeId)"))
				.setDescription(
						LocalizedText.english("Generate an Event with the TypeDefinition indicated by eventTypeId."))
				.build();

		final GenerateEventMethod generateEventMethod = new GenerateEventMethod(methodNode);
		methodNode.setInputArguments(generateEventMethod.getInputArguments());
		methodNode.setOutputArguments(generateEventMethod.getOutputArguments());
		methodNode.setInvocationHandler(generateEventMethod);

		getNodeManager().addNode(methodNode);

		methodNode.addReference(new Reference(methodNode.getNodeId(), Identifiers.HasComponent,
				folderNode.getNodeId().expanded(), false));
	}

	private void addScalarNodes(final UaFolderNode rootNode) {
		final UaFolderNode scalarTypesFolder = new UaFolderNode(getNodeContext(), newNodeId("HelloWorld/ScalarTypes"),
				newQualifiedName("ScalarTypes"), LocalizedText.english("ScalarTypes"));

		getNodeManager().addNode(scalarTypesFolder);
		rootNode.addOrganizes(scalarTypesFolder);

		for (final Object[] os : STATIC_SCALAR_NODES) {
			final String name = (String) os[0];
			final NodeId typeId = (NodeId) os[1];
			final Variant variant = (Variant) os[2];

			final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
					.setNodeId(newNodeId("HelloWorld/ScalarTypes/" + name)).setAccessLevel(AccessLevel.READ_WRITE)
					.setUserAccessLevel(AccessLevel.READ_WRITE).setBrowseName(newQualifiedName(name))
					.setDisplayName(LocalizedText.english(name)).setDataType(typeId)
					.setTypeDefinition(Identifiers.BaseDataVariableType).build();

			node.setValue(new DataValue(variant));

			node.getFilterChain().addLast(new AttributeLoggingFilter(AttributeId.Value::equals));

			getNodeManager().addNode(node);
			scalarTypesFolder.addOrganizes(node);
		}
	}

	private void addShutdownMethod(final UaFolderNode folderNode) {
		final UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
				.setNodeId(newNodeId(opcUaServer.getOpcUaServerConfiguration().getRootNodeId() + "/shutdown()"))
				.setBrowseName(newQualifiedName("shutdown()")).setDisplayName(new LocalizedText(null, "shutdown()"))
				.setDescription(LocalizedText.english("Shutdown the opcua server.")).build();

		final ShutdownMethod shutdownMethod = new ShutdownMethod(opcUaServer, methodNode);
		methodNode.setInputArguments(shutdownMethod.getInputArguments());
		methodNode.setOutputArguments(shutdownMethod.getOutputArguments());
		methodNode.setInvocationHandler(shutdownMethod);

		getNodeManager().addNode(methodNode);

		methodNode.addReference(
				new Reference(methodNode.getNodeId(), Identifiers.HasComponent, Identifiers.Server.expanded(), false));
	}

	private void addSqrtMethod(final UaFolderNode folderNode) {
		final UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/sqrt(x)")).setBrowseName(newQualifiedName("sqrt(x)"))
				.setDisplayName(new LocalizedText(null, "sqrt(x)"))
				.setDescription(
						LocalizedText.english("Returns the correctly rounded positive square root of a double value."))
				.build();

		final SqrtMethod sqrtMethod = new SqrtMethod(methodNode);
		methodNode.setInputArguments(sqrtMethod.getInputArguments());
		methodNode.setOutputArguments(sqrtMethod.getOutputArguments());
		methodNode.setInvocationHandler(sqrtMethod);

		getNodeManager().addNode(methodNode);

		methodNode.addReference(new Reference(methodNode.getNodeId(), Identifiers.HasComponent,
				folderNode.getNodeId().expanded(), false));
	}

	private void addVariableNodes(final UaFolderNode rootNode) {
		addArrayNodes(rootNode);
		addScalarNodes(rootNode);
		addAdminReadableNodes(rootNode);
		addAdminWritableNodes(rootNode);
		addDynamicNodes(rootNode);
		addDataAccessNodes(rootNode);
		addWriteOnlyNodes(rootNode);
	}

	private void addWriteOnlyNodes(final UaFolderNode rootNode) {
		final UaFolderNode writeOnlyFolder = new UaFolderNode(getNodeContext(), newNodeId("HelloWorld/WriteOnly"),
				newQualifiedName("WriteOnly"), LocalizedText.english("WriteOnly"));

		getNodeManager().addNode(writeOnlyFolder);
		rootNode.addOrganizes(writeOnlyFolder);

		final String name = "String";
		final UaVariableNode node = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
				.setNodeId(newNodeId("HelloWorld/WriteOnly/" + name)).setAccessLevel(AccessLevel.WRITE_ONLY)
				.setUserAccessLevel(AccessLevel.WRITE_ONLY).setBrowseName(newQualifiedName(name))
				.setDisplayName(LocalizedText.english(name)).setDataType(Identifiers.String)
				.setTypeDefinition(Identifiers.BaseDataVariableType).build();

		node.setValue(new DataValue(new Variant("can't read this")));

		getNodeManager().addNode(node);
		writeOnlyFolder.addOrganizes(node);
	}

	// TODO revisionare creazione namespace
	private void createAndAddNodes() {
		// Create a "HelloWorld" folder and add it to the node manager
		final NodeId folderNodeId = newNodeId(opcUaServer.getOpcUaServerConfiguration().getRootNodeId());

		final UaFolderNode folderNode = new UaFolderNode(getNodeContext(), folderNodeId,
				newQualifiedName(opcUaServer.getOpcUaServerConfiguration().getRootBrowseName()),
				LocalizedText.english(opcUaServer.getOpcUaServerConfiguration().getRootDisplayNameEnglish()));
		folderNode.setDescription(
				LocalizedText.english(opcUaServer.getOpcUaServerConfiguration().getRootDescriptionEnglish()));
		getNodeManager().addNode(folderNode);

		// Make sure our new folder shows up under the server's Objects folder.
		folderNode.addReference(new Reference(folderNode.getNodeId(), Identifiers.Organizes,
				Identifiers.ObjectsFolder.expanded(), false));
		addShutdownMethod(folderNode);
		// Add the rest of the nodes
		if (opcUaServer.getOpcUaServerConfiguration().enableDemoDatas()) {
			addVariableNodes(folderNode);

			addSqrtMethod(folderNode);

			addGenerateEventMethod(folderNode);

			try {
				registerCustomEnumType();
				addCustomEnumTypeVariable(folderNode);
			} catch (final Exception e) {
				logger.warn("Failed to register custom enum type", e);
			}

			try {
				registerCustomStructType();
				addCustomStructTypeVariable(folderNode);
			} catch (final Exception e) {
				logger.warn("Failed to register custom struct type", e);
			}

			try {
				registerCustomUnionType();
				addCustomUnionTypeVariable(folderNode);
			} catch (final Exception e) {
				logger.warn("Failed to register custom struct type", e);
			}

			addCustomObjectTypeAndInstance(folderNode);
		}
	}

	private void registerCustomEnumType() throws Exception {
		final NodeId dataTypeId = CustomEnumType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

		dictionaryManager.registerEnumCodec(new CustomEnumType.Codec().asBinaryCodec(), "CustomEnumType", dataTypeId);

		final UaNode node = getNodeManager().get(dataTypeId);
		if (node instanceof UaDataTypeNode) {
			final UaDataTypeNode dataTypeNode = (UaDataTypeNode) node;

			dataTypeNode.setEnumStrings(new LocalizedText[] { LocalizedText.english("Field0"),
					LocalizedText.english("Field1"), LocalizedText.english("Field2") });
		}

		final EnumField[] fields = new EnumField[] {
				new EnumField(0L, LocalizedText.english("Field0"), LocalizedText.NULL_VALUE, "Field0"),
				new EnumField(1L, LocalizedText.english("Field1"), LocalizedText.NULL_VALUE, "Field1"),
				new EnumField(2L, LocalizedText.english("Field2"), LocalizedText.NULL_VALUE, "Field2") };

		final EnumDefinition definition = new EnumDefinition(fields);

		final EnumDescription description = new EnumDescription(dataTypeId,
				new QualifiedName(getNamespaceIndex(), "CustomEnumType"), definition,
				ubyte(BuiltinDataType.Int32.getTypeId()));

		dictionaryManager.registerEnumDescription(description);
	}

	private void registerCustomStructType() throws Exception {
		// Get the NodeId for the DataType and encoding Nodes.

		final NodeId dataTypeId = CustomStructType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

		final NodeId binaryEncodingId = CustomStructType.BINARY_ENCODING_ID
				.toNodeIdOrThrow(getServer().getNamespaceTable());

		// At a minimum, custom types must have their codec registered.
		// If clients don't need to dynamically discover types and will
		// register the codecs on their own then this is all that is
		// necessary.
		// The dictionary manager will add a corresponding DataType Node to
		// the AddressSpace.

		dictionaryManager.registerStructureCodec(new CustomStructType.Codec().asBinaryCodec(), "CustomStructType",
				dataTypeId, binaryEncodingId);

		// If the custom type also needs to be discoverable by clients then it
		// needs an entry in a DataTypeDictionary that can be read by those
		// clients. We describe the type using StructureDefinition or
		// EnumDefinition and register it with the dictionary manager.
		// The dictionary manager will add all the necessary nodes to the
		// AddressSpace and generate the required dictionary bsd.xml file.

		final StructureField[] fields = new StructureField[] {
				new StructureField("foo", LocalizedText.NULL_VALUE, Identifiers.String, ValueRanks.Scalar, null,
						getServer().getConfig().getLimits().getMaxStringLength(), false),
				new StructureField("bar", LocalizedText.NULL_VALUE, Identifiers.UInt32, ValueRanks.Scalar, null,
						uint(0), false),
				new StructureField("baz", LocalizedText.NULL_VALUE, Identifiers.Boolean, ValueRanks.Scalar, null,
						uint(0), false),
				new StructureField("customEnumType", LocalizedText.NULL_VALUE,
						CustomEnumType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable()), ValueRanks.Scalar,
						null, uint(0), false) };

		final StructureDefinition definition = new StructureDefinition(binaryEncodingId, Identifiers.Structure,
				StructureType.Structure, fields);

		final StructureDescription description = new StructureDescription(dataTypeId,
				new QualifiedName(getNamespaceIndex(), "CustomStructType"), definition);

		dictionaryManager.registerStructureDescription(description, binaryEncodingId);
	}

	private void registerCustomUnionType() throws Exception {
		final NodeId dataTypeId = CustomUnionType.TYPE_ID.toNodeIdOrThrow(getServer().getNamespaceTable());

		final NodeId binaryEncodingId = CustomUnionType.BINARY_ENCODING_ID
				.toNodeIdOrThrow(getServer().getNamespaceTable());

		dictionaryManager.registerUnionCodec(new CustomUnionType.Codec().asBinaryCodec(), "CustomUnionType", dataTypeId,
				binaryEncodingId);

		final StructureField[] fields = new StructureField[] {
				new StructureField("foo", LocalizedText.NULL_VALUE, Identifiers.UInt32, ValueRanks.Scalar, null,
						getServer().getConfig().getLimits().getMaxStringLength(), false),
				new StructureField("bar", LocalizedText.NULL_VALUE, Identifiers.String, ValueRanks.Scalar, null,
						uint(0), false) };

		final StructureDefinition definition = new StructureDefinition(binaryEncodingId, Identifiers.Structure,
				StructureType.Union, fields);

		final StructureDescription description = new StructureDescription(dataTypeId,
				new QualifiedName(getNamespaceIndex(), "CustomUnionType"), definition);

		dictionaryManager.registerStructureDescription(description, binaryEncodingId);
	}

	private void startBogusEventNotifier() {
		if (getServer() != null && getServer().getEventFactory() != null) {
			// Set the EventNotifier bit on Server Node for Events.
			final UaNode serverNode = getServer().getAddressSpaceManager().getManagedNode(Identifiers.Server)
					.orElse(null);

			if (serverNode instanceof ServerTypeNode) {
				((ServerTypeNode) serverNode).setEventNotifier(ubyte(1));

				// Post a bogus Event every couple seconds
				eventThread = new Thread(() -> {
					while (keepPostingEvents) {
						try {
							final BaseEventTypeNode eventNode = getServer().getEventFactory()
									.createEvent(newNodeId(UUID.randomUUID()), Identifiers.BaseEventType);

							eventNode.setBrowseName(new QualifiedName(1, "foo"));
							eventNode.setDisplayName(LocalizedText.english("foo"));
							eventNode.setEventId(ByteString.of(new byte[] { 0, 1, 2, 3 }));
							eventNode.setEventType(Identifiers.BaseEventType);
							eventNode.setSourceNode(serverNode.getNodeId());
							eventNode.setSourceName(serverNode.getDisplayName().getText());
							eventNode.setTime(DateTime.now());
							eventNode.setReceiveTime(DateTime.NULL_VALUE);
							eventNode.setMessage(LocalizedText.english("event message!"));
							eventNode.setSeverity(ushort(2));

							// noinspection UnstableApiUsage
							getServer().getEventBus().post(eventNode);

							eventNode.delete();
						} catch (final Throwable e) {
							logger.error("Error creating EventNode: {}", e.getMessage(), e);
						}

						try {
							// noinspection BusyWait
							Thread.sleep(2_000);
						} catch (final InterruptedException ignored) {
							// ignored
						}
					}
				}, "bogus-event-poster");

				eventThread.start();
			}
		}
	}
}
