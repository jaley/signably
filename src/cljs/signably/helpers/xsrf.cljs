(ns signably.helpers.xsrf
  "Helper functions to work with XSRF token")

(defn get-token
  "Retrieve the XSRF Token from the hidden element so that it can be used
  in e.g. AJAX requests as a header/param."
  []
  (.. js/document
      (getElementById "__anti-forgery-token")
      -value))
