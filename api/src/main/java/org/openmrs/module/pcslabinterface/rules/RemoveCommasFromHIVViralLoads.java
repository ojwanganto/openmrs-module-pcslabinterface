package org.openmrs.module.pcslabinterface.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.pcslabinterface.PcsLabInterfaceConstants;
import org.springframework.util.StringUtils;

/**
 * Several numeric values for HIV Viral Load are simply digits with commas in
 * the thousands and millions places. These should be converted to valid HL7
 * numeric values by stripping the commas and adding a comment.
 * 
 * @author jkeiper
 */
public class RemoveCommasFromHIVViralLoads extends RegexTransformRule {

	// this regex ensures that the value has only digits and/or commas in it
	private Pattern valuePattern = Pattern
			.compile("OBX\\|\\d*\\|..\\|856\\^.+\\^99DCT\\|[^\\|]*\\|([^,\\|]*,[^\\|]*)\\|.*");

	/**
	 * initializes the regex pattern for matching on a specific concept
	 * 
	 * @should match numeric and structured text OBX segments for HIV Viral Load with commas in the value
	 * @should match values with other characters as long as there is at least one comma
	 * @should work for any OBX referencing 856 regardless of name
	 */
	public RemoveCommasFromHIVViralLoads() {
		// the follow regex ensures that the concept is HIV Viral Load and the
		// value has at least one comma in it
		super(
				"OBX\\|\\d*\\|..\\|856\\^.+\\^99DCT\\|[^\\|]*\\|[^,\\|]*,.*");
	}

	/**
	 * transforms the test string by stripping commas from the value and
	 * appending a comment (NTE segment) with the original value
	 * 
	 * @should remove commas from the original value
	 * @should add a comment containing the original value
	 */
	@Override
	public String transform(String test) {
		// check to make sure the value is truly just numbers and commas
		Matcher m = valuePattern.matcher(test);
		if (!m.matches())
			return test;

		// yank the value from the test string
		String value = m.group(1);

		// remove the commas
		String newValue = StringUtils.deleteAny(value, ",");

		// replace first occurrence of value with newValue
		test = test.replaceFirst(value, newValue);

		// append a comment describing the change
		return test.concat(PcsLabInterfaceConstants.MESSAGE_EOL_SEQUENCE)
				.concat("NTE|||")
				.concat(PcsLabInterfaceConstants.LAB_VALUE_MODIFIED)
				.concat(value);
	}
}
