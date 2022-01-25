/*
 * This class file was automatically generated by ASN1bean (http://www.beanit.com)
 */

package com.beanit.asn1bean.compiler.pkix1explicit88;

import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.io.Serializable;
import com.beanit.asn1bean.ber.*;
import com.beanit.asn1bean.ber.types.*;
import com.beanit.asn1bean.ber.types.string.*;


public class UniquePostalName extends PDSParameter {

	private static final long serialVersionUID = 1L;

	public UniquePostalName() {
	}

	public UniquePostalName(byte[] code) {
		super(code);
	}

	public UniquePostalName(BerPrintableString printableString, BerTeletexString teletexString) {
		super(printableString, teletexString);
	}

}
