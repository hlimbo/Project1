//<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
//<html xmlns="http://www.w3.org/1999/xhtml">
//  
//  
//
//
//  <head>
//    <title>
//      JDBC3.java on cs122b-2017-spring-project1 – Attachment
//     – Public
//    </title>
//    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
//    <!--[if IE]><script type="text/javascript">
//      if (/^#__msie303:/.test(window.location.hash))
//        window.location.replace(window.location.hash.replace(/^#__msie303:/, '#'));
//    </script><![endif]-->
//        <link rel="search" href="/wiki/public/search" />
//        <link rel="help" href="/wiki/public/wiki/TracGuide" />
//        <link rel="alternate" href="/wiki/public/raw-attachment/wiki/cs122b-2017-spring-project1/JDBC3.java" type="text/x-java; charset=iso-8859-15" title="Original Format" />
//        <link rel="up" href="/wiki/public/wiki/cs122b-2017-spring-project1" title="cs122b-2017-spring-project1" />
//        <link rel="start" href="/wiki/public/wiki" />
//        <link rel="stylesheet" href="/wiki/public/chrome/common/css/trac.css" type="text/css" /><link rel="stylesheet" href="/wiki/public/chrome/common/css/code.css" type="text/css" /><link rel="stylesheet" href="/wiki/public/chrome/tracwysiwyg/wysiwyg.css" type="text/css" />
//        <link rel="tracwysiwyg.stylesheet" href="/wiki/public/chrome/common/css/trac.css" /><link rel="tracwysiwyg.stylesheet" href="/wiki/public/chrome/tracwysiwyg/editor.css" />
//        <link rel="tracwysiwyg.base" href="/wiki/public" />
//        <link rel="shortcut icon" href="/wiki/public/chrome/site/favicon.ico" type="image/x-icon" />
//        <link rel="icon" href="/wiki/public/chrome/site/favicon.ico" type="image/x-icon" />
//    <script type="text/javascript" src="/wiki/public/chrome/common/js/jquery.js"></script><script type="text/javascript" src="/wiki/public/chrome/common/js/babel.js"></script><script type="text/javascript" src="/wiki/public/chrome/common/js/messages/en_US.js"></script><script type="text/javascript" src="/wiki/public/chrome/common/js/trac.js"></script><script type="text/javascript" src="/wiki/public/chrome/common/js/search.js"></script><script type="text/javascript" src="/wiki/public/chrome/tracwysiwyg/wysiwyg.js"></script><script type="text/javascript" src="/wiki/public/chrome/tracwysiwyg/wysiwyg-load.js"></script>
//    <!--[if lt IE 7]>
//    <script type="text/javascript" src="/wiki/public/chrome/common/js/ie_pre7_hacks.js"></script>
//    <![endif]-->
//      <script type="text/javascript" src="/wiki/public/chrome/common/js/folding.js"></script>
//      <script type="text/javascript">
//        jQuery(document).ready(function($) {
//          $('#preview table.code').enableCollapsibleColumns($('#preview table.code thead th.content'));
//        });
//      </script>
//  </head>
//  <body>
//    <div id="banner">
//      <div id="header">
//        <a id="logo" href="http://www.ics.uci.edu/"><img src="/wiki/public/chrome/site/ics.jpg" alt="ICS Logo" height="67" width="128" /></a>
//      </div>
//      <form id="search" action="/wiki/public/search" method="get">
//      </form>
//      <div id="metanav" class="nav">
//    <ul>
//      <li class="first"><a href="/wiki/public/login">Login</a></li><li><a href="/wiki/public/prefs">Preferences</a></li><li class="last"><a href="/wiki/public/about">About Trac</a></li>
//    </ul>
//  </div>
//    </div>
//    <div id="mainnav" class="nav">
//  </div>
//    <div id="main">
//      <div id="ctxtnav" class="nav">
//        <h2>Context Navigation</h2>
//          <ul>
//              <li class="last first"><a href="/wiki/public/wiki/cs122b-2017-spring-project1">Back to cs122b-2017-spring-project1</a></li>
//          </ul>
//        <hr />
//      </div>
//    <div id="content" class="attachment">
//        <h1><a href="/wiki/public/wiki/cs122b-2017-spring-project1">cs122b-2017-spring-project1</a>: JDBC3.java</h1>
//        <table id="info" summary="Description">
//          <tbody>
//            <tr>
//              <th scope="col">File JDBC3.java,
//                <span title="1361 bytes">1.3 KB</span>
//                (added by cluo8, <a class="timeline" href="/wiki/public/timeline?from=2017-04-03T11%3A22%3A37-07%3A00&amp;precision=second" title="2017-04-03T11:22:37-07:00 in Timeline">4 days</a> ago)</th>
//            </tr>
//            <tr>
//              <td class="message searchable">
//                
//              </td>
//            </tr>
//          </tbody>
//        </table>
//        <div id="preview" class="searchable">
//          
//  <table class="code"><thead><tr><th class="lineno" title="Line numbers">Line</th><th class="content"> </th></tr></thead><tbody><tr><th id="L1"><a href="#L1">1</a></th><td>// JDBC Example - updating a DB via SQL template and value groups</td></tr><tr><th id="L2"><a href="#L2">2</a></th><td>// Coded by Chen Li/Kirill Petrov Winter, 2005</td></tr><tr><th id="L3"><a href="#L3">3</a></th><td>// Slightly revised for ICS185 Spring 2005, by Norman Jacobson</td></tr><tr><th id="L4"><a href="#L4">4</a></th><td></td></tr><tr><th id="L5"><a href="#L5">5</a></th><td>import java.sql.*;                              // Enable SQL processing</td></tr><tr><th id="L6"><a href="#L6">6</a></th><td></td></tr><tr><th id="L7"><a href="#L7">7</a></th><td>public class JDBC3</td></tr><tr><th id="L8"><a href="#L8">8</a></th><td>{</td></tr><tr><th id="L9"><a href="#L9">9</a></th><td>    public static void main(String[] arg) throws Exception</td></tr><tr><th id="L10"><a href="#L10">10</a></th><td>    {</td></tr><tr><th id="L11"><a href="#L11">11</a></th><td>                // Incorporate MySQL driver</td></tr><tr><th id="L12"><a href="#L12">12</a></th><td>                 Class.forName("com.mysql.jdbc.Driver").newInstance();</td></tr><tr><th id="L13"><a href="#L13">13</a></th><td>        </td></tr><tr><th id="L14"><a href="#L14">14</a></th><td>                // Connect to the test database</td></tr><tr><th id="L15"><a href="#L15">15</a></th><td>                Connection connection =</td></tr><tr><th id="L16"><a href="#L16">16</a></th><td>                    DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&amp;useSSL=false", "mytestuser", "mypassword");</td></tr><tr><th id="L17"><a href="#L17">17</a></th><td>        </td></tr><tr><th id="L18"><a href="#L18">18</a></th><td>                // prepare SQL statement template that's to be repeatedly excuted</td></tr><tr><th id="L19"><a href="#L19">19</a></th><td>                String updateString = "update stars set first_name = ? where id = ?";</td></tr><tr><th id="L20"><a href="#L20">20</a></th><td>                PreparedStatement updateStars = connection.prepareStatement(updateString);</td></tr><tr><th id="L21"><a href="#L21">21</a></th><td>        </td></tr><tr><th id="L22"><a href="#L22">22</a></th><td>                // values for first and second "?" wildcard in statement template</td></tr><tr><th id="L23"><a href="#L23">23</a></th><td>                int [] ids = {755011, 755017};</td></tr><tr><th id="L24"><a href="#L24">24</a></th><td>                String [] firstNames = {"New Arnold", "New Eddie"};</td></tr><tr><th id="L25"><a href="#L25">25</a></th><td>        </td></tr><tr><th id="L26"><a href="#L26">26</a></th><td>                // for each record in table, update to new values given</td></tr><tr><th id="L27"><a href="#L27">27</a></th><td>                for(int i = 0; i &lt; ids.length; i++)</td></tr><tr><th id="L28"><a href="#L28">28</a></th><td>                    {</td></tr><tr><th id="L29"><a href="#L29">29</a></th><td>                        updateStars.setString(1, firstNames[i]);</td></tr><tr><th id="L30"><a href="#L30">30</a></th><td>                        updateStars.setInt(2, ids[i]);</td></tr><tr><th id="L31"><a href="#L31">31</a></th><td>                        updateStars.executeUpdate();</td></tr><tr><th id="L32"><a href="#L32">32</a></th><td>                    }</td></tr><tr><th id="L33"><a href="#L33">33</a></th><td>    }</td></tr><tr><th id="L34"><a href="#L34">34</a></th><td>}</td></tr></tbody></table>
//
//        </div>
//    </div>
//    <div id="altlinks">
//      <h3>Download in other formats:</h3>
//      <ul>
//        <li class="last first">
//          <a rel="nofollow" href="/wiki/public/raw-attachment/wiki/cs122b-2017-spring-project1/JDBC3.java">Original Format</a>
//        </li>
//      </ul>
//    </div>
//    </div>
//    <div id="footer" lang="en" xml:lang="en"><hr />
//      <a id="tracpowered" href="http://trac.edgewall.org/"><img src="/wiki/public/chrome/common/trac_logo_mini.png" height="30" width="107" alt="Trac Powered" /></a>
//      <p class="left">Powered by <a href="/wiki/public/about"><strong>Trac 0.12.5</strong></a><br />
//        By <a href="http://www.edgewall.org/">Edgewall Software</a>.</p>
//      <p class="right">Visit the Trac open source project at<br /><a href="http://trac.edgewall.org/">http://trac.edgewall.org/</a></p>
//    </div>
//  </body>
//</html>