/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.github.capure.schema;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class TestCaseEvent extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -5592393269468421071L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TestCaseEvent\",\"namespace\":\"io.github.capure.schema\",\"fields\":[{\"name\":\"type\",\"type\":{\"type\":\"enum\",\"name\":\"TestCaseEventType\",\"symbols\":[\"ADD\",\"DELETE\"]}},{\"name\":\"details\",\"type\":{\"type\":\"record\",\"name\":\"TestCaseEventDetails\",\"fields\":[{\"name\":\"id\",\"type\":\"long\"},{\"name\":\"problemId\",\"type\":\"long\"},{\"name\":\"name\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"input\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"output\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"maxScore\",\"type\":\"int\",\"default\":0}]}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<TestCaseEvent> ENCODER =
      new BinaryMessageEncoder<TestCaseEvent>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<TestCaseEvent> DECODER =
      new BinaryMessageDecoder<TestCaseEvent>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<TestCaseEvent> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<TestCaseEvent> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<TestCaseEvent>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this TestCaseEvent to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a TestCaseEvent from a ByteBuffer. */
  public static TestCaseEvent fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  @Deprecated public io.github.capure.schema.TestCaseEventType type;
  @Deprecated public io.github.capure.schema.TestCaseEventDetails details;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public TestCaseEvent() {}

  /**
   * All-args constructor.
   * @param type The new value for type
   * @param details The new value for details
   */
  public TestCaseEvent(io.github.capure.schema.TestCaseEventType type, io.github.capure.schema.TestCaseEventDetails details) {
    this.type = type;
    this.details = details;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return type;
    case 1: return details;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: type = (io.github.capure.schema.TestCaseEventType)value$; break;
    case 1: details = (io.github.capure.schema.TestCaseEventDetails)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'type' field.
   * @return The value of the 'type' field.
   */
  public io.github.capure.schema.TestCaseEventType getType() {
    return type;
  }

  /**
   * Sets the value of the 'type' field.
   * @param value the value to set.
   */
  public void setType(io.github.capure.schema.TestCaseEventType value) {
    this.type = value;
  }

  /**
   * Gets the value of the 'details' field.
   * @return The value of the 'details' field.
   */
  public io.github.capure.schema.TestCaseEventDetails getDetails() {
    return details;
  }

  /**
   * Sets the value of the 'details' field.
   * @param value the value to set.
   */
  public void setDetails(io.github.capure.schema.TestCaseEventDetails value) {
    this.details = value;
  }

  /**
   * Creates a new TestCaseEvent RecordBuilder.
   * @return A new TestCaseEvent RecordBuilder
   */
  public static io.github.capure.schema.TestCaseEvent.Builder newBuilder() {
    return new io.github.capure.schema.TestCaseEvent.Builder();
  }

  /**
   * Creates a new TestCaseEvent RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new TestCaseEvent RecordBuilder
   */
  public static io.github.capure.schema.TestCaseEvent.Builder newBuilder(io.github.capure.schema.TestCaseEvent.Builder other) {
    return new io.github.capure.schema.TestCaseEvent.Builder(other);
  }

  /**
   * Creates a new TestCaseEvent RecordBuilder by copying an existing TestCaseEvent instance.
   * @param other The existing instance to copy.
   * @return A new TestCaseEvent RecordBuilder
   */
  public static io.github.capure.schema.TestCaseEvent.Builder newBuilder(io.github.capure.schema.TestCaseEvent other) {
    return new io.github.capure.schema.TestCaseEvent.Builder(other);
  }

  /**
   * RecordBuilder for TestCaseEvent instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<TestCaseEvent>
    implements org.apache.avro.data.RecordBuilder<TestCaseEvent> {

    private io.github.capure.schema.TestCaseEventType type;
    private io.github.capure.schema.TestCaseEventDetails details;
    private io.github.capure.schema.TestCaseEventDetails.Builder detailsBuilder;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(io.github.capure.schema.TestCaseEvent.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.type)) {
        this.type = data().deepCopy(fields()[0].schema(), other.type);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.details)) {
        this.details = data().deepCopy(fields()[1].schema(), other.details);
        fieldSetFlags()[1] = true;
      }
      if (other.hasDetailsBuilder()) {
        this.detailsBuilder = io.github.capure.schema.TestCaseEventDetails.newBuilder(other.getDetailsBuilder());
      }
    }

    /**
     * Creates a Builder by copying an existing TestCaseEvent instance
     * @param other The existing instance to copy.
     */
    private Builder(io.github.capure.schema.TestCaseEvent other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.type)) {
        this.type = data().deepCopy(fields()[0].schema(), other.type);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.details)) {
        this.details = data().deepCopy(fields()[1].schema(), other.details);
        fieldSetFlags()[1] = true;
      }
      this.detailsBuilder = null;
    }

    /**
      * Gets the value of the 'type' field.
      * @return The value.
      */
    public io.github.capure.schema.TestCaseEventType getType() {
      return type;
    }

    /**
      * Sets the value of the 'type' field.
      * @param value The value of 'type'.
      * @return This builder.
      */
    public io.github.capure.schema.TestCaseEvent.Builder setType(io.github.capure.schema.TestCaseEventType value) {
      validate(fields()[0], value);
      this.type = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'type' field has been set.
      * @return True if the 'type' field has been set, false otherwise.
      */
    public boolean hasType() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'type' field.
      * @return This builder.
      */
    public io.github.capure.schema.TestCaseEvent.Builder clearType() {
      type = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'details' field.
      * @return The value.
      */
    public io.github.capure.schema.TestCaseEventDetails getDetails() {
      return details;
    }

    /**
      * Sets the value of the 'details' field.
      * @param value The value of 'details'.
      * @return This builder.
      */
    public io.github.capure.schema.TestCaseEvent.Builder setDetails(io.github.capure.schema.TestCaseEventDetails value) {
      validate(fields()[1], value);
      this.detailsBuilder = null;
      this.details = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'details' field has been set.
      * @return True if the 'details' field has been set, false otherwise.
      */
    public boolean hasDetails() {
      return fieldSetFlags()[1];
    }

    /**
     * Gets the Builder instance for the 'details' field and creates one if it doesn't exist yet.
     * @return This builder.
     */
    public io.github.capure.schema.TestCaseEventDetails.Builder getDetailsBuilder() {
      if (detailsBuilder == null) {
        if (hasDetails()) {
          setDetailsBuilder(io.github.capure.schema.TestCaseEventDetails.newBuilder(details));
        } else {
          setDetailsBuilder(io.github.capure.schema.TestCaseEventDetails.newBuilder());
        }
      }
      return detailsBuilder;
    }

    /**
     * Sets the Builder instance for the 'details' field
     * @param value The builder instance that must be set.
     * @return This builder.
     */
    public io.github.capure.schema.TestCaseEvent.Builder setDetailsBuilder(io.github.capure.schema.TestCaseEventDetails.Builder value) {
      clearDetails();
      detailsBuilder = value;
      return this;
    }

    /**
     * Checks whether the 'details' field has an active Builder instance
     * @return True if the 'details' field has an active Builder instance
     */
    public boolean hasDetailsBuilder() {
      return detailsBuilder != null;
    }

    /**
      * Clears the value of the 'details' field.
      * @return This builder.
      */
    public io.github.capure.schema.TestCaseEvent.Builder clearDetails() {
      details = null;
      detailsBuilder = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TestCaseEvent build() {
      try {
        TestCaseEvent record = new TestCaseEvent();
        record.type = fieldSetFlags()[0] ? this.type : (io.github.capure.schema.TestCaseEventType) defaultValue(fields()[0]);
        if (detailsBuilder != null) {
          record.details = this.detailsBuilder.build();
        } else {
          record.details = fieldSetFlags()[1] ? this.details : (io.github.capure.schema.TestCaseEventDetails) defaultValue(fields()[1]);
        }
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<TestCaseEvent>
    WRITER$ = (org.apache.avro.io.DatumWriter<TestCaseEvent>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<TestCaseEvent>
    READER$ = (org.apache.avro.io.DatumReader<TestCaseEvent>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}