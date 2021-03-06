package com.interact.listen

import org.joda.time.Duration
import org.joda.time.LocalTime
import org.joda.time.Period
import org.joda.time.format.PeriodFormatter
import org.joda.time.format.PeriodFormatterBuilder

class DateTimeTagLib {
    static namespace = 'listen'

    def prettyduration = { attrs ->
        if(!attrs.duration) throwTagError 'Tag [prettyduration] is missing required attribute [duration]'

        def formatter = new PeriodFormatterBuilder()
            .printZeroNever()
            .appendHours()
            .appendSuffix('h ')
            .printZeroNever()
            .appendMinutes()
            .appendSuffix('m ')
            .printZeroRarelyLast()
            .minimumPrintedDigits(1)
            .appendSeconds()
            .appendSuffix('s')
            .toFormatter()
        out << formatter.print(attrs.duration.toPeriod())
    }

    def computeDuration = { attrs ->
        if((!attrs.start) && (!attrs.end)) {
            out << "0.00:00"
            return
        }
        if(!attrs.start)throwTagError 'Tag [computeduration] is missing required attribute [start]'
        if(!attrs.end)  throwTagError 'Tag [computeduration] is missing required attribute [end]'

        PeriodFormatter fmt = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(1)
                .appendHours()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(":")
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
                .toFormatter();

        out << fmt.print(new Period(attrs.start, attrs.end))
    }

    def formatduration = { attrs ->
        if(!attrs.duration) throwTagError 'Tag [formatduration] is missing required attribute [duration]'
        boolean millis = attrs.containsKey('millis') ? Boolean.valueOf(attrs.millis) : false

        Duration tmpDuration = attrs.duration
        def milliSecs = tmpDuration.millis % 1000

        if ((!millis) && (milliSecs > 500)){
            // If we don't want to display milliseconds, we'll round if necessary
            tmpDuration = tmpDuration.plus(1000 - milliSecs)   // This should round up to the nearest second
        }

        def builder = builderFor(tmpDuration.millis)

        if(millis) {
            builder.appendSeparator('.')
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendMillis3Digit()
        }

        out << builder.toFormatter().print(tmpDuration.toPeriod())
    }

    private def builderFor(long millis) {
        final long MILLIS_PER_MINUTE = 1000 * 60
        final long MILLIS_PER_TEN_MINUTES = MILLIS_PER_MINUTE * 10
        final long MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60

        if(millis < MILLIS_PER_MINUTE) {
            // '0:00' to '0:59'
            return new PeriodFormatterBuilder()
                .appendLiteral('0:00:')
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
        } else if(millis < MILLIS_PER_TEN_MINUTES) {
            // '1:00' to '9:59'
            return new PeriodFormatterBuilder()
                .printZeroNever()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(':')
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
        } else if(millis < MILLIS_PER_HOUR) {
            // '10:00' to '59:59'
            return new PeriodFormatterBuilder()
                .printZeroNever()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSeparator(':')
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
        } else {
            // '1:00:00' and above, e.g. '12:34:56', '123:45:54'
            return new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(1)
                .appendHours()
                .appendSuffix(':')
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendMinutes()
                .appendSuffix(':')
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendSeconds()
        }
    }

    def timePicker = { attrs ->
        def name = attrs.name
        def id = attrs.id ?: name
        def value = attrs.value
        if(value == 'none') {
            value = null
        } else if(!value) {
            value = new LocalTime()
        }

        out << '<input type="hidden" name="' + name + '" value="struct"/>'
        out << '<select name="' + name + '_hour" id="' + id + '_hour"/>'
        for(i in 0..23) {
            def h = i.toString().padLeft(2, '0')
            out << '<option value="' + h + '"' + (value?.hourOfDay == i ? ' selected="selected"' : '') + '>' + h + '</option>'
        }
        out << '</select>'


        out << '<select name="' + name + '_minute" id="' + id + '_minute"/>'
        for(i in [0, 15, 30, 45, 59]) {
            def m = i.toString().padLeft(2, '0')
            out << '<option value="' + m + '"' + (value?.minuteOfHour == i ? ' selected="selected"' : '') + '>' + m + '</option>'
        }
        out << '</select>'
    }
}
