(ns ^:no-doc portal.runtime.index)

(defn html [{:keys [name version host session-id code-url platform mode]
             :or   {name       "portal"
                    version    "0.25.0"
                    code-url   "main.js"
                    platform   #?(:bb "bb" :clj "jvm" :cljs "node")}}]
  (str
   "<!DOCTYPE html>"
   "<html lang=\"en\">"
   "<head>"
   "<title>" (str name " - " platform " - " version) "</title>"
   "<meta charset='UTF-8' />"
   "<meta name='viewport' content='width=device-width, initial-scale=1' />"
   "<link rel=\"icon\" href=\"/icon.svg\">"
   "</head>"
   "<body style=\"margin: 0; padding: 0; overflow: hidden\">"
   "<div id=\"root\"></div>"
   "<script>"
   (when host
     (str "window.PORTAL_HOST    = " (pr-str host) ";"))
   (when session-id
     (str "window.PORTAL_SESSION = " (pr-str session-id) ";"))
   "</script>"
   "<script src=\"" code-url "\"></script>"
   ;; wait.js will ensure headless chrome doesn't exit early
   (when (= mode :test) "<script src=\"wait.js\"></script>")
   "</body>"
   "</html>"))
