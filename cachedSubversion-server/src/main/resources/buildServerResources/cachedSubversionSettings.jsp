<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<tr>
    <td colspan="2">
        <em>Caches subversion repository on agent for faster checkout. <b>Manual checkout mode on vcs tab is
            required!</b></em>
    </td>
</tr>

<tr class="noBorder cachedSubversion">
    <th>Checkout mode:</th>
    <td>
        <props:selectProperty id="cachedSubversion.mode" name="cachedSubversion.mode"
                              onchange="BS.cachedSubversion.update()">
            <props:option value="Checkout">Checkout/update</props:option>
            <props:option value="Export">Export</props:option>
        </props:selectProperty>
        <span class="smallNote">
            <b>Checkout/update</b> - checkout/update after swabra cleanup. <b>.svn</b> directory will be in working directory.<br/>
            <b>Export</b> - Export directory from repository. Working copy will be without <b>.svn</b> directory, updates will be slower. Deleted in repository files will remain intact<br/><br/>
    </span>
    </td>
</tr>
<tr class="noBorder cachedSubversion-checkout">
    <th>Revert before update:</th>
    <td>
        <props:checkboxProperty name="cachedSubversion.revert" value="true"/>
        <span class="smallNote">Revert directory before swabra cleanup. Recommends instead of swabra cleanup</span>
    </td>
</tr>
<tr class="noBorder cachedSubversion-checkout">
    <th>Cleanup before update:</th>
    <td>
        <props:checkboxProperty name="cachedSubversion.clean" value="true"/>
        <span class="smallNote">Cleanup directory before swabra cleanup. Recommends instead of swabra cleanup</span>
    </td>
</tr>
<tr class="noBorder cachedSubversion-export">
    <th>Delete before export:</th>
    <td>
        <props:checkboxProperty name="cachedSubversion.delete" value="true"/>
        <span class="smallNote">Delete directory before export</span>
    </td>
</tr>
<script type="text/javascript">
    BS.cachedSubversion = {
        update: function () {
            var val = jQuery("select[name='prop:cachedSubversion.mode']").val();
            if (val == "Checkout") {
                jQuery(".cachedSubversion-export").hide();
                jQuery(".cachedSubversion-checkout").show();
            } else {
                jQuery(".cachedSubversion-export").show();
                jQuery(".cachedSubversion-checkout").hide();
            }
        }
    }

    BS.cachedSubversion.update()
</script>