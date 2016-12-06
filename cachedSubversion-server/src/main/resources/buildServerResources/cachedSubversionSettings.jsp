<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<tr>
    <td colspan="2">
        <em>Caches subversion repository on agent for faster checkout. <b>Manual checkout mode on vcs tab is
            required!</b></em>
    </td>
</tr>

<tr class="noBorder">
    <th>Checkout mode:</th>
    <td>
        <props:selectProperty name="cachedSubversion.mode">
            <props:option value="revertCheckout">revert and checkout/update</props:option>
            <props:option value="checkout">checkout/update</props:option>
            <props:option value="deleteExport">delete and export</props:option>
            <props:option value="export">export</props:option>
        </props:selectProperty>
        <span class="smallNote">
            <b>Revert and checkout/update</b> - revert directory before swabra cleanup and checkout/update after swabra cleanup. <b>.svn</b> directory will be in working copy<br/><br/>
            <b>Checkout/update</b> - checkout/update after swabra cleanup. <b>.svn</b> directory will be in working copy. All changes will remain intact, there may be conflicts<br/><br/>
            <b>Delete and export</b> - delete folder and export from repository. Working copy will be without <b>.svn</b> directory, updates will be slower<br/>Checkout rules to file always use this value<br/><br/>
            <b>Export</b> - Export directory from repository. Working copy will be without <b>.svn</b> directory, updates will be slower. Deleted in repository files will remain intact<br/><br/>
    </span>
    </td>
</tr>