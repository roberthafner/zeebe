package org.camunda.tngp.bpmn.graph.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.tngp.bpmn.graph.validation.ValidationCodes.PROCESS_START_EVENT_UNSUPPORTED;
import static org.camunda.tngp.bpmn.graph.validation.ValidationResultsAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.ProcessBuilder;
import org.camunda.bpm.model.xml.validation.ModelElementValidator;
import org.camunda.bpm.model.xml.validation.ValidationResults;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class ProcessStartEventSupportedTypesValidatorTest
{
    Collection<ModelElementValidator<?>> validators;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        validators = Arrays.asList(new ProcessStartEventSupportedTypesValidator());
    }

    @Test
    public void shouldReportUnsupportedStartEvents()
    {
        // given
        final ProcessBuilder processBuilder = Bpmn.createExecutableProcess();
        processBuilder.startEvent("messageStartEvent").message("someMessage");
        processBuilder.startEvent();
        final BpmnModelInstance modelInstance = processBuilder.done();

        // if
        final ValidationResults results = modelInstance.validate(validators);

        // then
        assertThat(results.getErrorCount()).isEqualTo(1);
        assertThat(results).element("messageStartEvent").hasError(PROCESS_START_EVENT_UNSUPPORTED);
    }

}