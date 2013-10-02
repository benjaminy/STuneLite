/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.record;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Title:        Bound Sheet Record (aka BundleSheet) <P>
 * Description:  Defines a sheet within a workbook.  Basically stores the sheetname
 *               and tells where the Beginning of file record is within the HSSF
 *               file. <P>
 * REFERENCE:  PG 291 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Sergei Kozello (sergeikozello at mail.ru)
 * @version 2.0-pre
 */
public final class BoundSheetRecord extends Record {
	public final static short sid = 0x0085;

	private static final BitField hiddenFlag = BitFieldFactory.getInstance(0x01);
	private static final BitField veryHiddenFlag = BitFieldFactory.getInstance(0x02);
	private int field_1_position_of_BOF;
	private int field_2_option_flags;
	private int field_4_isMultibyteUnicode;
	private String field_5_sheetname;

	public BoundSheetRecord(String sheetname) {
		field_2_option_flags = 0;
		setSheetname(sheetname);
	}

	/**
	 * UTF8: sid + len + bof + flags + len(str) + unicode + str 2 + 2 + 4 + 2 +
	 * 1 + 1 + len(str)
	 * 
	 * UNICODE: sid + len + bof + flags + len(str) + unicode + str 2 + 2 + 4 + 2 +
	 * 1 + 1 + 2 * len(str)
	 * 
	 */
	public BoundSheetRecord(RecordInputStream in) {
		field_1_position_of_BOF = in.readInt();
		field_2_option_flags = in.readUShort();
		int field_3_sheetname_length = in.readUByte();
		field_4_isMultibyteUnicode = in.readByte();

		if (isMultibyte()) {
			field_5_sheetname = in.readUnicodeLEString(field_3_sheetname_length);
		} else {
			field_5_sheetname = in.readCompressedUnicode(field_3_sheetname_length);
		}
	}

	/**
	 * set the offset in bytes of the Beginning of File Marker within the HSSF
	 * Stream part of the POIFS file
	 * 
	 * @param pos
	 *			offset in bytes
	 */
	public void setPositionOfBof(int pos) {
		field_1_position_of_BOF = pos;
	}

	/**
	 * Set the sheetname for this sheet.  (this appears in the tabs at the bottom)
	 * @param sheetName the name of the sheet
	 * @throws IllegalArgumentException if sheet name will cause excel to crash. 
	 */
	public void setSheetname(String sheetName) {
		
		validateSheetName(sheetName);
		field_5_sheetname = sheetName;
		field_4_isMultibyteUnicode = StringUtil.hasMultibyte(sheetName) ?  1 : 0;
	}

	private static void validateSheetName(String sheetName) {
		if (sheetName == null) {
			throw new IllegalArgumentException("sheetName must not be null");
		}
		int len = sheetName.length();
		if (len < 1) {
			throw new IllegalArgumentException("sheetName must not be empty string");
		}
		for (int i=0; i<len; i++) {
			char ch = sheetName.charAt(i);
			switch (ch) {
				case '/':
				case '\\':
				case '?':
				case '*':
				case ']':
				case '[':
					break;
				default:
					// all other chars OK
					continue;
			}
			throw new IllegalArgumentException("Invalid char (" + ch 
					+ ") found at index (" + i + ") in sheet name '" + sheetName + "'");
		}
 	}

	/**
	 * get the offset in bytes of the Beginning of File Marker within the HSSF Stream part of the POIFS file
	 *
	 * @return offset in bytes
	 */
	public int getPositionOfBof() {
		return field_1_position_of_BOF;
	}

	private boolean isMultibyte() {
		return (field_4_isMultibyteUnicode & 0x01) != 0;
	}

	/**
	 * get the sheetname for this sheet.  (this appears in the tabs at the bottom)
	 * @return sheetname the name of the sheet
	 */
	public String getSheetname() {
		return field_5_sheetname;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[BOUNDSHEET]\n");
		buffer.append("    .bof        = ").append(HexDump.intToHex(getPositionOfBof())).append("\n");
		buffer.append("    .options    = ").append(HexDump.shortToHex(field_2_option_flags)).append("\n");
		buffer.append("    .unicodeflag= ").append(HexDump.byteToHex(field_4_isMultibyteUnicode)).append("\n");
		buffer.append("    .sheetname  = ").append(field_5_sheetname).append("\n");
		buffer.append("[/BOUNDSHEET]\n");
		return buffer.toString();
	}
	
	private int getDataSize() {
		return 8 + field_5_sheetname.length() * (isMultibyte() ? 2 : 1);
	}

	public int serialize(int offset, byte[] data) {
		int dataSize = getDataSize();
		LittleEndian.putUShort(data, 0 + offset, sid);
		LittleEndian.putUShort(data, 2 + offset, dataSize);
		LittleEndian.putInt(data, 4 + offset, getPositionOfBof());
		LittleEndian.putUShort(data, 8 + offset, field_2_option_flags);

		String name = field_5_sheetname;
		LittleEndian.putByte(data, 10 + offset, name.length());
		LittleEndian.putByte(data, 11 + offset, field_4_isMultibyteUnicode);

		if (isMultibyte()) {
			StringUtil.putUnicodeLE(name, data, 12 + offset);
		} else {
			StringUtil.putCompressedUnicode(name, data, 12 + offset);
		}
		return 4 + dataSize;
	}

	public int getRecordSize() {
		return 4 + getDataSize();
	}

	public short getSid() {
		return sid;
	}

	/**
	 * Is the sheet hidden? Different from very hidden 
	 */
	public boolean isHidden() {
		return hiddenFlag.isSet(field_2_option_flags);
	}

	/**
	 * Is the sheet hidden? Different from very hidden 
	 */
	public void setHidden(boolean hidden) {
		field_2_option_flags = hiddenFlag.setBoolean(field_2_option_flags, hidden);
	}

	/**
	 * Is the sheet very hidden? Different from (normal) hidden 
	 */
	public boolean isVeryHidden() {
		return veryHiddenFlag.isSet(field_2_option_flags);
	}

	/**
	 * Is the sheet very hidden? Different from (normal) hidden 
	 */
	public void setVeryHidden(boolean veryHidden) {
		field_2_option_flags = veryHiddenFlag.setBoolean(field_2_option_flags, veryHidden);
	}
	
	/**
	 * Converts a List of {@link BoundSheetRecord}s to an array and sorts by the position of their
	 * BOFs.
	 */
	public static BoundSheetRecord[] orderByBofPosition(List boundSheetRecords) {
		BoundSheetRecord[] bsrs = new BoundSheetRecord[boundSheetRecords.size()];
		boundSheetRecords.toArray(bsrs);
		Arrays.sort(bsrs, BOFComparator);
	 	return bsrs;
	}
	private static final Comparator BOFComparator = new Comparator() {
		public int compare(Object bsr1, Object bsr2) {
			return compare((BoundSheetRecord)bsr1, (BoundSheetRecord)bsr2);
		}
		public int compare(BoundSheetRecord bsr1, BoundSheetRecord bsr2) {
			return bsr1.getPositionOfBof() - bsr2.getPositionOfBof();
		}
	};
}