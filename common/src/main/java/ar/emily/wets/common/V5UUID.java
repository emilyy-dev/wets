package ar.emily.wets.common;

import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static java.lang.invoke.MethodHandles.byteArrayViewVarHandle;

// why am i overcomplicating this
final class V5UUID {

  private static final VarHandle AS_LONG_ARRAY = byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);
  private static final MessageDigest MD;

  static {
    try {
      MD = MessageDigest.getInstance("SHA-1");
    } catch (final NoSuchAlgorithmException ex) {
      // shouldn't throw, SHA-1 is a standard algorithm that is required by Java SE
      // https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html
      throw new ExceptionInInitializerError(ex);
    }
  }

  // MessageDigest isn't thread safe, that's why it's being cloned
  @SuppressWarnings("AccessToNonThreadSafeStaticField")
  private static MessageDigest md() {
    try {
      return (MessageDigest) MD.clone();
    } catch (final CloneNotSupportedException ex) {
      // shouldn't throw
      throw new RuntimeException(ex);
    }
  }

  // use V5 UUID as to not collide with V3 or V4 UUIDs, which are used by the game
  static UUID create(final byte[] bs) {
    final var md = md();
    md.update(bs);
    final byte[] digest = md.digest();
    digest[6] &= 0x0f; // clear version
    digest[6] |= 0x50; // set version 5
    digest[8] &= 0x3f; // clear variant
    digest[8] |= (byte) 0x80; // set variant 1
    return new UUID(
        (long) AS_LONG_ARRAY.get(digest, 0),
        (long) AS_LONG_ARRAY.get(digest, 8)
    );
  }

  private V5UUID() {
  }
}
