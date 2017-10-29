
/*
 * Copy summary input to name input
 */
(function( $ ) {
    $.fn.copyInputTo = function(target) {
        this.change(function(ev) {
            $(target).val( $(this).val().toLowerCase().replace(/ /g, '_').replace(/[^a-z0-9_-]/g, ''));
        });
        this.keyup(function(ev) {
            $(target).val( $(this).val().toLowerCase().replace(/ /g, '_').replace(/[^a-z0-9_-]/g, ''));
        });
        return this;
    };
}( jQuery ));


/*
 * Copy summary input to name input
 */
(function( $ ) {
    $.fn.addAnotherField = function(inputName) {
        this.click(function(ev) {
        ev.preventDefault();
            // the input array zero element
            var inputZero = $('#' + inputName + '_0');
            var id = parseInt(inputZero.attr('data-fieldcount'));
            inputZero.attr('data-fieldcount', id + 1);
            // clone the li and append
            var sel = inputZero.clone();
            $(sel).attr('id', inputName + '_' + id);
            $(sel).attr('name', inputName + '[' + id + ']');
            $(sel).attr('data-fieldcount', '');
            var li = document.createElement('li');
            var a = document.createElement('a');
            $(a).attr('class', 'remove');
            $(a).text('Remove');
            $(a).click(function(ev) {
                ev.preventDefault();
                $(this).parent().remove();
            });
            $(li).append(sel);
            $(li).append(a);
            $(this).parent().parent().append(li);
        });
        return this;
    };
}( jQuery ));

/*
 * Object name validator
 */
(function( $ ) {
    $.fn.checkObjectName = function(target, objectType) {
        // event handler
        var handler = function(ev)
        {
            var name = $(this).val();
            if (name && name.length > 2)
            {
                $.getJSON('/api/config/exists/' + encodeURI(objectType) + '/' + encodeURI(name), function(data) {
                    if (data)
                    {
                        $(target).css('color', '#E85752');
                        $(target).attr('title', 'Boo :(, name already in use');
                    }
                    else
                    {
                        $(target).css('color', '#00BF00');
                        $(target).attr('title', 'Yay :), name not in use');
                    }
                });
            }
        };
        // setup the events
        return this.each(function(i, e) {
            $(e).keyup(handler);
            $(e).change(handler);
        });
    };
}( jQuery ));
 
