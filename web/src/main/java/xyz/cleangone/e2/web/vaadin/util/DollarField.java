package xyz.cleangone.e2.web.vaadin.util;

import com.vaadin.data.Binder;
import com.vaadin.data.Result;
import com.vaadin.data.ValidationException;
import com.vaadin.data.ValueContext;
import com.vaadin.data.converter.StringToBigDecimalConverter;
import com.vaadin.ui.TextField;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DollarField extends TextField
{
    Binder<DollarHolder> binder = new Binder<>();

    public DollarField(String caption)
    {
        this(caption, "$");
    }
    public DollarField(String caption, String placeholder)
    {
        super(caption);
        setPlaceholder(placeholder);

        binder.forField(this)
            .withConverter(new DollarConverter())
            .bind(DollarHolder::getBd, DollarHolder::setBd);
    }

    public BigDecimal getDollarValue()
    {
        DollarHolder holder = new DollarHolder();
        try
        {
            binder.writeBean(holder);
            return holder.getBd();
        }
        catch (ValidationException e)
        {
            return null;
        }
    }

    class DollarHolder
    {
        BigDecimal bd;
        BigDecimal getBd()
        {
            return bd;
        }
        void setBd(BigDecimal bd)
        {
            if (bd == null) { bd = new BigDecimal(0); }
            this.bd = bd.setScale(2, RoundingMode.CEILING);
        }
    }

    class DollarConverter extends StringToBigDecimalConverter
    {
        DollarConverter()
        {
            super("Invalid format");
        } // error message

        @Override
        public Result<BigDecimal> convertToModel(String value, ValueContext context)
        {
            String dollarValue = value;
            if (dollarValue != null && dollarValue.startsWith("$")) { dollarValue = dollarValue.substring(1); }
            return super.convertToModel(dollarValue, context);
        }
    }
}
