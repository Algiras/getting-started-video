(ns app.ui
  (:require [om.dom :as dom]
            [om.next :as om :refer-macros [defui]]
            yahoo.intl-messageformat-with-locales
            [untangled.client.core :as uc]
            [untangled.client.mutations :as m]))

(defui ^:once Item
  static uc/InitialAppState
  (initial-state [clz {:keys [id label]}] {:id id :label label})
  static om/IQuery
  (query [this] [:id :label])
  static om/Ident
  (ident [this {:keys [id]}] [:items/by-id id])
  Object
  (render [this]
    (let [{:keys [label]} (om/props this)]
      (dom/li nil label))))

(def ui-item (om/factory Item {:keyfn :label}))

(defui ^:once MyList
  static uc/InitialAppState
  (initial-state [clz params] {:title             "Initial List"
                               :ui/new-item-label ""
                               :items             []})
  static om/IQuery
  (query [this] [:ui/new-item-label :title {:items (om/get-query Item)}])
  static om/Ident
  (ident [this {:keys [title]}] [:lists/by-title title])
  Object
  (render [this]
    (let [{:keys [title items ui/new-item-label] :or {ui/new-item-label ""}} (om/props this)]
      (dom/div nil
        (dom/h4 nil title)
        (dom/input #js {:value    new-item-label
                        :onChange (fn [evt] (m/set-string! this :ui/new-item-label :event evt))})
        (dom/button #js {:onClick #(do
                                    (m/set-string! this :ui/new-item-label :value "")
                                    (om/transact! this `[(app/add-item {:id ~(om/tempid) :label ~new-item-label})
                                                         (untangled/load {:query         [{:all-items ~(om/get-query Item)}]
                                                                          :post-mutation fetch/items-loaded})
                                                         ]))} "+")
        (dom/ul nil
          (map ui-item items))))))

(def ui-list (om/factory MyList))

(defui ^:once Root
  static uc/InitialAppState
  (initial-state [clz params] {:list (uc/initial-state MyList {})})
  static om/IQuery
  (query [this] [:ui/react-key :ui/loading-data {:list (om/get-query MyList)}])
  Object
  (render [this]
    (let [{:keys [react-key ui/loading-data list]} (om/props this)]
      (dom/div #js {:key react-key}
        (dom/h4 nil "Header" (when loading-data " (LOADING)"))
        (ui-list list)))))
